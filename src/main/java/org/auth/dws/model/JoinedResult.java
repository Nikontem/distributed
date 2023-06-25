package org.auth.dws.model;

import java.time.LocalDate;

public record JoinedResult(String key, LocalDate smallDatasetDate, LocalDate bigDatasetDate) {}
