package cs2.dma.main;

import cs2.dma.tuil.GameDataManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        GameDataManager manager = new GameDataManager();

        if (manager.initializeVmm()) {
            System.out.println("[+] Game data manager initialized!");

            if (manager.initializeGameData()) {
                System.out.println("[+] Game data initialized!");

                GameDataController.setGameDataManager(manager);
                SpringApplication.run(Application.class, args);
            } else {
                handleInitializationError(
                        "[-] Failed to initialize game data.");
            }
        } else {
            handleInitializationError(
                    "[-] Failed to initialize VMM.");
        }
    }

    private static void handleInitializationError(String errorMessage) {
        System.out.println("[-] Initialization Error: " + errorMessage);
        System.exit(1);
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(50);
        factory.setReadTimeout(50);
        return factory;
    }
}
