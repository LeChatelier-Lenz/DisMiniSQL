package MasterManagers.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PublicIPFetcher {
    public static String getPublicIP() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.ipify.org"))
                .timeout(java.time.Duration.ofSeconds(5))  // 超时设置
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String ip = response.body().trim();
                if (ip.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {  // 简单校验
                    return ip;
                }
            }
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
        }
        return "获取失败";
    }

    public static void main(String[] args) {
        System.out.println("公网 IP: " + getPublicIP());
    }
}