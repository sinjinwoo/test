package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class GitHubAuthController {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1️⃣ 로그인 진입
    @GetMapping("/login/github")
    public RedirectView redirectToGitHub() {
        String githubAuthUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId
                + "&scope=repo user";
        return new RedirectView(githubAuthUrl);
    }

    // 2️⃣ GitHub 콜백 → 액세스 토큰 교환
    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> githubCallback(@RequestParam String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> params = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                request,
                Map.class
        );

        String accessToken = (String) response.getBody().get("access_token");
        return ResponseEntity.ok(Map.of("access_token", accessToken));
    }
}
