# EduPOP automated tests

This branch includes a layered test suite so the main regression checks do not
require manually opening every page.

## Run locally

From the `EduPOP` directory:

```bash
mvn clean verify
```

The HTML coverage report is generated at:

```text
target/site/jacoco/index.html
```

## What is checked

| Layer | Coverage |
|---|---|
| Application context | Spring Bean wiring, configuration properties, MyBatis mapper XML and ResultMap loading |
| Security contract | Every Controller mapping is discovered automatically; anonymous and wrong-role access are checked |
| Authentication | Signup role, duplicate login ID, login failure, withdrawn account, active/pending role routing, withdrawal |
| Account | Account lookup/edit, local/social metadata, session refresh, validation errors |
| Classroom | Single/bulk status changes, teacher removal, student assignment/removal/synchronization |
| Teacher exams | Teacher ownership, list/detail/create/update guard, WORD/normal PDF parser routing |
| Student exams | List/paging, WORD exam, review, submit, results, growth summary, today's review |
| OMR | Exam sheet creation, session-owned teacher/academy IDs, bulk grades, statistics, teacher comments |
| Reports | Monthly student report, reflection updates, parent report trend data, shared-link device authentication |

Controller and service unit tests use mocks. The full context test uses an
in-memory H2 database and dummy external API values, so tests do not modify the
developer's MySQL data and do not call Kakao, Naver, Google, or NTS APIs.

## Automatic execution

GitHub Actions runs `mvn clean verify` on pushes to `main` and
`merge-main-seoyoung-auth`, and on pull requests. The JaCoCo HTML report is
uploaded as the `jacoco-report` workflow artifact.
