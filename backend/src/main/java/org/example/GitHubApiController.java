package org.example;

import org.kohsuke.github.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubApiController {

    @GetMapping("/me/repos")
    public List<String> getMyRepos(@RequestHeader("Authorization") String token) throws IOException, IOException {
        String accessToken = token.replace("Bearer ", "");

        GitHub github = new GitHubBuilder()
                .withOAuthToken(accessToken)
                .build();

        return github.getMyself()
                .listRepositories()
                .toList()
                .stream()
                .map(GHRepository::getFullName)
                .toList();
    }
    //[
    //    "hy2min/march_study",
    //    "hy2min/tikkletikkle",
    //    "Johndoe557/partyduo",
    //    "sinjinwoo/algo",
    //    "sinjinwoo/algorithm_study",
    //    "sinjinwoo/date-box",
    //    "sinjinwoo/devcoach_back",
    //    "sinjinwoo/devcoach_front",
    //    "sinjinwoo/evNovel",
    //    "sinjinwoo/feb_study",
    //    "sinjinwoo/maplestory.openapi",
    //    "sinjinwoo/partyduo",
    //    "sinjinwoo/partyduo_develope",
    //    "sinjinwoo/productive-box",
    //    "sinjinwoo/sinjinwoo",
    //    "sinjinwoo/ssafy_test",
    //    "sinjinwoo/t1",
    //    "sinjinwoo/test",
    //    "sinjinwoo/tikkletikkle"
    //]

    @GetMapping("/repos/{owner}/{repo}/branches")
    public List<String> getBranches(@RequestHeader("Authorization") String token,
                                    @PathVariable String owner,
                                    @PathVariable String repo) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHRepository repository = github.getRepository(owner + "/" + repo);
        return repository.getBranches().keySet().stream().toList();
    }
    //[
    //    "cookie",
    //    "main"
    //]

    @GetMapping("/repos/{owner}/{repo}/commits")
    public List<String> getCommits(@RequestHeader("Authorization") String token,
                                   @PathVariable String owner,
                                   @PathVariable String repo) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHRepository repository = github.getRepository(owner + "/" + repo);
        return repository.listCommits()
                .toList()
                .stream()
                .map(GHCommit::getSHA1)
                .toList();
    }

    //[
    //    "3ca7fdd7da49bd34e8e81c607749d8c284de5178",
    //    "effac946f63ae8c3b4f0fbd19bc2fd42f834a732",
    //    "570dc9da83ffec8213e76507359aba8c2d93176d",
    //    "1dc5a7d2e881893805b6b6f67c64878f93d8e878",
    //    "4ab6b297bff31c0afdd4d353939bb24e2802fefa",
    //    "d788043f1a3ae6f92f8913015e18b3577b40b3b1"
    //]

    @PostMapping("/repos/{owner}/{repo}/prs")
    public Map<String, String> createPR(@RequestHeader("Authorization") String token,
                                        @PathVariable String owner,
                                        @PathVariable String repo,
                                        @RequestBody Map<String, String> body) throws IOException {
        String title = body.get("title");
        String head = body.get("head"); // ex: feature/login
        String base = body.get("base"); // ex: main
        String description = body.get("body");

        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHRepository repository = github.getRepository(owner + "/" + repo);

        GHPullRequest pr = repository.createPullRequest(title, head, base, description);
        return Map.of("url", pr.getHtmlUrl().toString());
    }

    // {
    //    "url": "https://github.com/sinjinwoo/t1/pull/2"
    //}

    @GetMapping("/repos/{owner}/{repo}/prs")
    public List<Map<String, Object>> getAllPRs(@RequestHeader("Authorization") String token,
                                               @PathVariable String owner,
                                               @PathVariable String repo) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHRepository repository = github.getRepository(owner + "/" + repo);

        return repository.getPullRequests(GHIssueState.ALL).stream().map(pr -> {
            Map<String, Object> map = new HashMap<>();
            map.put("number", pr.getNumber());
            map.put("title", pr.getTitle());
            map.put("state", pr.getState().toString());
            map.put("head", pr.getHead().getRef());
            map.put("base", pr.getBase().getRef());
            return map;
        }).toList();
    }
    //[
    //    {
    //        "head": "testbranch",
    //        "number": 2,
    //        "state": "OPEN",
    //        "title": "PR 제목",
    //        "base": "main"
    //    },
    //    {
    //        "head": "createapp",
    //        "number": 1,
    //        "state": "CLOSED",
    //        "title": "앱 생성",
    //        "base": "main"
    //    }
    //]

    @GetMapping("/repos/{owner}/{repo}/can-pr")
    public Map<String, Object> canCreatePR(@RequestHeader("Authorization") String token,
                                           @PathVariable String owner,
                                           @PathVariable String repo,
                                           @RequestParam String head,  // 예: feature/login
                                           @RequestParam String base)  // 예: main
            throws IOException {

        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHRepository repository = github.getRepository(owner + "/" + repo);

        try {
            // 비교: base ← head
            GHCompare compare = repository.getCompare(base, head);
            int aheadBy = compare.getAheadBy();  // head가 base보다 몇 커밋 앞서는지

            return Map.of(
                    "canCreatePR", aheadBy > 0,
                    "aheadBy", aheadBy,
                    "behindBy", compare.getBehindBy(),
                    "status", compare.getStatus()
            );
        } catch (GHFileNotFoundException e) {
            return Map.of("canCreatePR", false, "error", "One of the branches doesn't exist");
        }
    }
    //{
    //    "canCreatePR": true,
    //    "aheadBy": 1,
    //    "behindBy": 3,
    //    "status": "diverged"
    //}

    @GetMapping("/repos/{owner}/{repo}/prs/{number}")
    public Map<String, Object> getPR(@RequestHeader("Authorization") String token,
                                     @PathVariable String owner,
                                     @PathVariable String repo,
                                     @PathVariable int number) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHPullRequest pr = github.getRepository(owner + "/" + repo).getPullRequest(number);

        return Map.of(
                "title", pr.getTitle(),
                "body", pr.getBody(),
                "user", pr.getUser().getLogin(),
                "state", pr.getState().toString()
        );
    }
    //{
    //    "title": "PR 제목",
    //    "state": "OPEN",
    //    "body": "PR 설명입니다.",
    //    "user": "sinjinwoo"
    //}

    @GetMapping("/repos/{owner}/{repo}/prs/{number}/mergeable")
    public Map<String, Boolean> isMergeable(@RequestHeader("Authorization") String token,
                                            @PathVariable String owner,
                                            @PathVariable String repo,
                                            @PathVariable int number) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHPullRequest pr = github.getRepository(owner + "/" + repo).getPullRequest(number);

        return Map.of(
                "mergeable", pr.getMergeable(),
                "merged", pr.isMerged()
        );
    }
    //{
    //    "mergeable": true,
    //    "merged": false
    //}

    //변경된 파일 목록
    @GetMapping("/repos/{owner}/{repo}/prs/{number}/files")
    public List<Map<String, String>> getPRFiles(@RequestHeader("Authorization") String token,
                                                @PathVariable String owner,
                                                @PathVariable String repo,
                                                @PathVariable int number) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHPullRequest pr = github.getRepository(owner + "/" + repo).getPullRequest(number);

        List<GHPullRequestFileDetail> files = pr.listFiles().toList();

        return files.stream().map(f -> Map.of(
                "filename", f.getFilename(),
                "status", f.getStatus(),  // modified, added, removed
                "patch", f.getPatch()     // diff 내용
        )).toList();
    }

    //pr속 커밋 목록
    @GetMapping("/repos/{owner}/{repo}/prs/{number}/commits")
    public List<Map<String, String>> getPRCommits(@RequestHeader("Authorization") String token,
                                                  @PathVariable String owner,
                                                  @PathVariable String repo,
                                                  @PathVariable int number) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHPullRequest pr = github.getRepository(owner + "/" + repo).getPullRequest(number);

        return pr.listCommits().toList().stream().map(commit -> Map.of(
                "sha", commit.getSha(),
                "message", commit.getCommit().getMessage(),
                "author", commit.getCommit().getAuthor().getName()
        )).toList();
    }

    //커밋 파일 내부 내용보기
    @GetMapping("/repos/{owner}/{repo}/commits/{sha}/files")
    public List<Map<String, String>> getCommitFiles(@RequestHeader("Authorization") String token,
                                                    @PathVariable String owner,
                                                    @PathVariable String repo,
                                                    @PathVariable String sha) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token.replace("Bearer ", "")).build();
        GHCommit commit = github.getRepository(owner + "/" + repo).getCommit(sha);

        return commit.getFiles().stream().map(f -> Map.of(
                "filename", f.getFileName(),
                "status", f.getStatus(),
                "patch", f.getPatch()
        )).toList();
    }


}
