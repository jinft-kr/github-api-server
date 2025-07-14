# github-api-server
## 프로젝트 설명
- GitHub API를 활용하여 GitHub 저장소 (Repository) 와 사용자 (User) 에 대한 다양한 정보를 제공하는 API를 제공합니다.
- PAT (Personal Access Token) 를 사용하여 API 요청하는 경우 Request header에 PAT값을 추가하여 요청합니다.
- Container 기반으로 어플리케이션을 실행하며, EKS에 구축한 github actions를 통하여 자동 빌드 및 배포가 이뤄집니다.

## 프로젝트 구조
``` 
├── .github/ # GitHub Actions 관련 설정
│ └── workflows/ 
│           deployment.yaml
│ └── manifests/
│           deploy-to-eks.yml
├── gradle/
│ └── wrapper/
├── script/ # CI/CD 도구의 배포를 위한 스크립트
│ └── manifests/
│           runner-deployment.yml
│ └── setup-arc.sh
├── src/ # 소스 코드 폴더
├── build.gradle
├── settings.gradle
├── Dockerfile # 도커 이미지 생성을 위한 설정 파일
├── gradlew
├── gradlew.bat
└── README.md
```
## GitHub REST API 를 활용한 API 서버
### 로컬에서 서버 실행하기
```angular2html
docker build -t github-api-server .
docker run -d -p 8080:8080 --name github-api-server github-api-server
```

### Swageer UI 접속하기
```angular2html
http://localhost:8080/swagger-ui/index.html
```
- 저장소 활동 API
  - endpoint : GET /api/repos/{owner}/{repo}/summary
- 사용자 프로필 분석 API
  - endpoint: GET /api/users/{username}/profile-summary
- 인기 저장소 API
  - endpoint: GET /api/popular-repo
  - query parameter: (필수) owner - 조직 이름, (선택) limit - 반환 저장소 최대 개수

## 나만의 CI/CD 만들기
GitHub 저장소에 코드가 Push 되거나 Pull Request 병합이 될 때마다, Github Actions를 통행 자동으로 빌드 및 배포가 이뤄집니다.
