package cs2.dma.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketController {

	@Autowired
	private SimpMessagingTemplate template;

	@Autowired
	private GameDataController gameDataController;

	private ScheduledExecutorService executorService;

	@EventListener
	public void handleSessionConnectedEvent(SessionConnectedEvent event) {
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(() -> {
				String gameData;
				try {
					gameData = gameDataController.getGameData();
					if (!gameData.isEmpty()) {
						template.convertAndSend("/topic/radar", gameData);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 0, 5, TimeUnit.MILLISECONDS);
		}
	}
}