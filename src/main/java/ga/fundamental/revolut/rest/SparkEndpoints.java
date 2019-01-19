package ga.fundamental.revolut.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ga.fundamental.revolut.exception.MalformedRequestException;
import ga.fundamental.revolut.model.ApiError;
import ga.fundamental.revolut.service.AccountService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

import static spark.Spark.*;

@Slf4j
public class SparkEndpoints {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Inject
    public SparkEndpoints(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
        createEndpoints();
    }

    private void createEndpoints() {
        port(8080);
        path("/accounts", () -> {
            get("/list", this::listAll);
            get(("/:id"), this::getById);
            put("/create", this::createAccount);
            delete("/:id", this::deleteAccount);
            post("/deposit", this::deposit);
            post("/withdraw", this::withdraw);
            post("/transfer", this::transfer);
        });

        //exception handling
        ExceptionHandler<Exception> malformedRequestHandler = (e, request, response) -> {
            response.status(400);
            response.body(writeValueAsString(ApiError.of(e)));
        };
        ExceptionHandler<Exception> internalServerErrorHandler = (e, request, response) -> {
            response.status(500);
            response.body(writeValueAsString(ApiError.of(e)));
        };
        exception(NumberFormatException.class, malformedRequestHandler);
        exception(MalformedRequestException.class, malformedRequestHandler);
        exception(RuntimeException.class, internalServerErrorHandler);
    }

    private String getById(Request request, Response response) {
        Long id = Long.valueOf(request.params(":id"));
        return writeValueAsString(accountService.get(id));
    }

    private String listAll(Request request, Response response) {
        return writeValueAsString(accountService.getAll());
    }

    private String createAccount(Request request, Response response) {
        BigDecimal amount = new BigDecimal(Optional.ofNullable(readField(request.body(), "amount")).orElseThrow(MalformedRequestException::new));
        return writeValueAsString(accountService.create(amount));
    }

    private String deleteAccount(Request request, Response response) {
        Long id = Long.valueOf(request.params(":id"));
        return writeValueAsString(accountService.delete(id));
    }

    private String deposit(Request request, Response response) {
        Long id = Long.valueOf(Optional.ofNullable(readField(request.body(), "id")).orElseThrow(MalformedRequestException::new));
        BigDecimal amount = new BigDecimal(Optional.ofNullable(readField(request.body(), "amount")).orElseThrow(MalformedRequestException::new));
        return writeValueAsString(accountService.deposit(id, amount));
    }

    private String withdraw(Request request, Response response) {
        Long id = Long.valueOf(Optional.ofNullable(readField(request.body(), "id")).orElseThrow(MalformedRequestException::new));
        BigDecimal amount = new BigDecimal(Optional.ofNullable(readField(request.body(), "amount")).orElseThrow(MalformedRequestException::new));
        return writeValueAsString(accountService.withdraw(id, amount));
    }

    private String transfer(Request request, Response response) {
        Long fromId = Long.valueOf(Optional.ofNullable(readField(request.body(), "fromId")).orElseThrow(MalformedRequestException::new));
        Long toId = Long.valueOf(Optional.ofNullable(readField(request.body(), "toId")).orElseThrow(MalformedRequestException::new));
        BigDecimal amount = new BigDecimal(Optional.ofNullable(readField(request.body(), "amount")).orElseThrow(MalformedRequestException::new));
        return writeValueAsString(accountService.transfer(fromId, toId, amount));
    }

    private String readField(@NonNull String json, @NonNull String name) {
        try {
            ObjectNode object = objectMapper.readValue(json, ObjectNode.class);
            JsonNode node = object.get(name);
            return (node == null ? null : node.textValue());
        } catch (Exception e) {
            throw new MalformedRequestException("Failed to read json string " + json);
        }
    }

    private String writeValueAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to write object as json", e);
            return null;
        }
    }
}
