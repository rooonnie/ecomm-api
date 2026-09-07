package com.rooonnie.ecomm.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplacePriceBreaksRequest(@NotEmpty List<@Valid PriceBreakRequest> breaks) {
}
