package com.bilicraft.whocraftit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;
import java.util.UUID;
@AllArgsConstructor
@Builder
@Data
public class DataContainer {
    @NonNull
    private UUID crafter;
    @NonNull
    private Map<UUID, Long> useDamage;
}
