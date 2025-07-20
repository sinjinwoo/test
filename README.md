## ✅ 주요 기능

| 기능 | URL | 메서드 |
|------|-----|--------|
| 내 레포 목록 조회 | `/api/github/me/repos` | `GET` |
| 특정 레포 브랜치 목록 | `/api/github/repos/{owner}/{repo}/branches` | `GET` |
| 특정 레포 커밋 목록 | `/api/github/repos/{owner}/{repo}/commits` | `GET` |
| PR 생성 | `/api/github/repos/{owner}/{repo}/prs` | `POST` |
| 모든 PR 목록 조회 | `/api/github/repos/{owner}/{repo}/prs` | `GET` |
| PR 상세 정보 | `/api/github/repos/{owner}/{repo}/prs/{number}` | `GET` |
| PR 병합 가능 여부 확인 | `/api/github/repos/{owner}/{repo}/prs/{number}/mergeable` | `GET` |
| PR 변경 파일 목록 | `/api/github/repos/{owner}/{repo}/prs/{number}/files` | `GET` |
| PR 내 커밋 목록 | `/api/github/repos/{owner}/{repo}/prs/{number}/commits` | `GET` |
| 특정 커밋 변경 파일 | `/api/github/repos/{owner}/{repo}/commits/{sha}/files` | `GET` |
| PR 생성 가능 여부 확인 (브랜치 비교) | `/api/github/repos/{owner}/{repo}/can-pr?head={branch}&base={branch}` | `GET` |

---

## 🔐 인증 방법

모든 요청에는 **GitHub Personal Access Token (PAT)** 을 헤더에 포함시켜야 합니다:
