# ADR-0005: Replace the WAF + EJB tier with Spring MVC + Spring Security

## Status
Accepted

## Context
The legacy storefront runs on a bespoke "Web Application Framework" (WAF): a front-controller
servlet, an XML-driven screen-flow engine, and a request→event→action pattern, sitting on top of
EJB session facades (`ShoppingClientController`), CMP entity beans, JNDI service lookups, and a
container-managed `SignOnFilter` with a CMP `User` entity. All of it is `javax.*`, J2EE-1.3-era,
and depends on a full application-server container.

## Decision
Replace the entire web + EJB machinery with idiomatic Spring:

| Legacy | Replacement |
|--------|-------------|
| WAF `MainServlet` + `screen-flow.xml` | Spring MVC `@Controller`s + view resolution |
| Request events / `HTMLAction` | Controller methods |
| EJB session facade (`ShoppingClientController`) | Application `@Service`s |
| CMP entity beans | JPA entities (ADR-0003/0004) |
| `ServiceLocator` + JNDI | Spring dependency injection |
| `SignOnFilter` + CMP `User` (plaintext-ish) | Spring Security form login + BCrypt |
| Container-managed transactions | `@Transactional` |
| JSP + tag libs | Thymeleaf |

## Consequences
- Namespace migration `javax.*` → `jakarta.*` throughout (Servlet, Persistence, Validation).
- No application server required — an embedded servlet container in an executable jar.
- **Security is intentionally upgraded**, not reproduced bug-for-bug: passwords are BCrypt-hashed.
  This is a conscious deviation from strict parity (see risk R4) and is the responsible choice.
- The screen-flow's declarative navigation is re-expressed as controller routing; the flow logic
  is documented per slice so the mapping stays traceable.
