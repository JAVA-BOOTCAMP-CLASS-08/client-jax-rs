package ar.com.sicos;

import ar.com.sicos.model.Operation;
import ar.com.sicos.model.OperationInput;
import ar.com.sicos.model.OperationOutput;
import ar.com.sicos.utils.JsonHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
@SpringBootApplication
public class Application  implements CommandLineRunner {

	@Value("${service.path}")
	private String path;

	@Autowired
	private JsonHandler handler;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public void run(String... args) {
		RestTemplate template = new RestTemplate();

		Integer resultado = template.getForObject(path + "/suma/10/20", Integer.class);

		log.info("{} + {} = {}", 10, 20, resultado);

		ResponseEntity<Integer> entity = template.getForEntity(path + "/suma/10/20", Integer.class);
		log.info("Status {} -> {} + {} = {}", entity.getStatusCode(), 10, 20, entity.getBody());

		entity = template.getForEntity(path + "/division/10/5", Integer.class);
		log.info("Status {} -> {} / {} = {}", entity.getStatusCode(), 10, 5, entity.getBody());

		try {
			entity = template.getForEntity(path + "/division/10/0", Integer.class);
			log.info("Status {} -> {} / {} = {}", entity.getStatusCode(), 10, 0, entity.getBody());
		} catch (RestClientException exc) {
			log.error("{} / {} -> {}", 10, 0, exc.getMessage());
		}

		OperationInput input = new OperationInput();
		input.setOperation(Operation.PRODUCTO);
		input.setValue1(10);
		input.setValue2(5);

		Map<String, Object> m = handler.toMap(input);

		HttpEntity<OperationInput> request = new HttpEntity<>(input);

		ResponseEntity<OperationOutput> response = template.exchange(path + "/operacion", HttpMethod.POST, request, OperationOutput.class);

		log.info("Status {} -> {} * {} = {}", response.getStatusCode(), input.getValue1(), input.getValue2(), Objects.requireNonNull(response.getBody()).getResultado());

		JsonNode jNode = handler.toJsonNode(response.getBody());

		m = handler.toMap(response);

		log.info(jNode.toString());



	}
}
