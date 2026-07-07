# ADR-0006: Order submission port replaces the JMS→OPC hand-off

## Status
Accepted

## Context
In the legacy system the storefront does **not** persist orders. At checkout it serializes a
purchase order to XML and publishes it asynchronously via **JMS** to the **Order Processing
Center (OPC)** EAR, which owns order state, fulfilment, and admin. The OPC/supplier/admin apps
are out of scope for this migration.

This leaves two gaps: (1) there is no OPC to receive messages, and (2) a storefront that forgets
every order the instant it is placed is a poor demo and an odd modern default.

## Decision
Introduce an outbound **`OrderSubmissionPort`** in the `order` context:

- Checkout **persists the order locally** (new behavior — durable order records), then calls
  `OrderSubmissionPort.submit(order)`.
- The default adapter **records/logs** the submission — a faithful stand-in for "handed off to
  OPC," with no external broker required.
- A real integration (JMS, Kafka, or HTTP to a fulfilment service) can be added later as an
  alternative adapter with **zero change to storefront logic**.

## Consequences
- The legacy asynchronous, fire-and-forget intent is preserved as an explicit seam rather than
  erased.
- We add order persistence the legacy storefront lacked — called out as an intentional
  enhancement (risk R8), not an accidental behavior change.
- The demo runs with no external message broker; the anti-corruption boundary is real and
  visible, which is exactly the integration story a large migration must tell.
