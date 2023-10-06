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
            if (manager.initializeGameData()) {
                GmaeDataController.setGameDataManager(manager);
                SpringApplication.run(Application.class, args);
            }
        }
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
