# Skill Registry — bspapeleria-backend

## Project Metadata
- **Project**: bspapeleria-backend
- **Root**: `bspapeleria-backend/`
- **Stack**: Spring Boot 3.2.5 + Java 17, Spring Security + JWT (jjwt 0.12.5), Spring Data JPA, PostgreSQL/H2, Lombok, Validation, Actuator
- **Build**: Maven
- **Persistence Mode**: engram

## SDD Phase Skills
| Phase     | Skill                  | Trigger                                    |
|-----------|------------------------|--------------------------------------------|
| `init`    | `sdd-init`             | `sdd init`, `openspec init`                |
| `explore` | `sdd-explore`          | Exploration, requirement clarification      |
| `propose` | `sdd-propose`          | Change proposal with intent/scope/approach  |
| `spec`    | `sdd-spec`             | Write delta specs with requirements/scenarios |
| `design`  | `sdd-design`          | Technical design and architecture          |
| `tasks`   | `sdd-tasks`            | Break change into implementation tasks      |
| `apply`   | `sdd-apply`           | Implement SDD tasks from specs/design       |
| `verify`  | `sdd-verify`          | Execute tests, prove implementation matches |
| `archive` | `sdd-archive`         | Sync delta specs after completion           |
| `onboard` | `sdd-onboard`         | Walkthrough full SDD cycle on real codebase|

## Stacked PR Skills
| Skill          | Trigger                                      |
|----------------|----------------------------------------------|
| `chained-pr`   | PRs > 400 lines, stacked PRs, review slices |
| `branch-pr`    | PRs with issue-first checks                  |

## Supplementary Skills
| Skill                    | Trigger                              |
|--------------------------|--------------------------------------|
| `spring-boot-best-practices` | Spring Boot backend best practices |
| `supabase-render-springboot` | Supabase + Render + Spring Boot |
| `fullstack-nextjs-springboot-supabase` | Fullstack Next.js + Spring Boot + Supabase |
| `go-testing`             | Go tests, coverage, golden files      |
| `comment-writer`         | PR feedback, reviews, collaboration   |
| `issue-creation`         | GitHub issues, bug reports            |
| `judgment-day`           | Blind dual review, adversarial review |
| `work-unit-commits`      | Commit planning, review units         |
| `skill-registry`         | After skill changes, update registry  |

## Testing Capabilities
- **Runner**: Maven (`mvn test`)
- **Test Dependencies**: `spring-boot-starter-test`, `spring-security-test`
- **Strict TDD**: `false` — no test files detected in `src/test/`
- **Coverage**: Not configured
- **Linter**: Not detected
- **Formatter**: Not detected

## SDD Workflow Order
```
explore → propose → spec → design → tasks → apply → verify → archive
```
Onboarding: `sdd-onboard` runs the full cycle on the real codebase.