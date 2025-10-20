package huy.example.demoMonday.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FiveScoresBulkUpsertReq(
        @NotNull List<@Valid FiveScoresUpsertReq> items
) {}
