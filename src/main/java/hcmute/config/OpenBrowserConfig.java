package hcmute.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OpenBrowserConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8080";
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                // Lệnh mở trình duyệt mặc định trên Windows
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // Lệnh mở trên MacOS
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Lệnh mở trên Linux
                rt.exec("xdg-open " + url);
            }
        } catch (Exception e) {
            System.out.println("Không thể tự động mở trình duyệt: " + e.getMessage());
        }
    }
}