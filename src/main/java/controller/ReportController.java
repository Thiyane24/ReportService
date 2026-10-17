package controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/reports") // Note: No StripPrefix filter in GateX for this route, so we keep /api
public class ReportController {

    @GetMapping("/fast")
    public Map<String, String> getFastReport() {
        return Map.of("status", "Success", "data", "This is a fast response.");
    }

    @GetMapping("/slow")
    public Map<String, String> getSlowReport() throws InterruptedException {
        // Simulate a heavy database query or a frozen service by sleeping for 5 seconds.
        // GateX is configured to timeout after 3 seconds, so this should trigger a 504 error.
        Thread.sleep(5000);
        return Map.of("status", "Success", "data", "This response is too late.");
    }
}