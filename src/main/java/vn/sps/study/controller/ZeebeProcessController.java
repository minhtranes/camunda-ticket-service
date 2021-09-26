package vn.sps.study.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.protobuf.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import vn.sps.study.service.IdService;

@RestController
@Slf4j
@RequestMapping
public class ZeebeProcessController {

	@Autowired
	private ZeebeClient client;

	@Autowired
	private IdService idService;

	@PostMapping("/processes/{processId}/start")
	public void request(
	        @PathVariable(name = "ticketId", required = false) String ticketId,
	        @RequestParam(name = "type", required = false, defaultValue = "Facility") String type,
	        @RequestParam(name = "amount", required = false, defaultValue = "1") int amount,
	        @RequestParam(name = "totalCostAmount", required = false, defaultValue = "500") int totalCostAmount,
	        @PathVariable(name = "processId", required = true) String processId,
	        @RequestBody(required = false) String envelope) {

		if (ticketId == null) {
			ticketId = idService.next("TicketId");
		}

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("ticketId", ticketId);
		variables.put("type", type);
		variables.put("amount", amount);
		variables.put("totalCostAmount", totalCostAmount);
		variables.put("envelope", envelope);

		client.newCreateInstanceCommand().bpmnProcessId(processId)
		        .latestVersion().variables(variables).send();
		log.info(
		        "Request a ticket with id = {}, type = {}, amount = {}, totalCostAmount = {}",
		        ticketId, type, amount, totalCostAmount);
	}

	@PostMapping("/messages/{messageName}/start")
	public void message(
			@PathVariable(name = "messageName", required = true) String messageName,
			@RequestHeader Map<String, Object> headers,
			@RequestBody Map<String, Object> bodyParts) {

		String eventId = (String) bodyParts.get("eventId");

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.putAll(headers);
		variables.putAll(bodyParts);

		String correlationKey = UUID.randomUUID().toString();
		client.newPublishMessageCommand()
				.messageName(messageName)
				.correlationKey(eventId)


				.variables(variables).send();
		log.info(
				"Message from {} with correlation key {}", messageName,eventId );
	}

}