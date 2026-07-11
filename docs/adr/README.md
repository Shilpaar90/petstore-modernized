# Architecture Decision Records

Each ADR captures one significant decision, its context, and its consequences. Format follows
Michael Nygard's template. ADRs are immutable once accepted; a reversal is a new ADR that
supersedes the old one.

| # | Decision | Status |
|---|----------|--------|
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-target-spring-boot-3-5-on-java-21.md) | Target Spring Boot 3.5 on Java 21 (defer 4.x) | Accepted |
| [0003](0003-hexagonal-architecture-and-persistence-ports.md) | Hexagonal architecture with persistence ports | Accepted |
| [0004](0004-relational-first-then-mongodb.md) | Relational-first, then MongoDB behind the same ports | Accepted |
| [0005](0005-replace-waf-and-ejb-with-spring-mvc-and-security.md) | Replace WAF + EJB with Spring MVC + Spring Security | Accepted |
| [0006](0006-order-submission-port-replaces-jms-opc.md) | Order submission port replaces JMS→OPC hand-off | Accepted |
| [0007](0007-package-as-executable-jar-and-docker-compose.md) | Package as an executable jar with an optional Docker Compose stack | Accepted |
