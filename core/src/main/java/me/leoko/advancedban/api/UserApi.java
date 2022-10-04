package me.leoko.advancedban.api;

import me.leoko.advancedban.utils.Punishment;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface UserApi {
    List<Punishment> fetchActivePunishments(UUID uuid) throws SQLException;

    List<Punishment> fetchHistoryPunishments(UUID uuid) throws SQLException;
}
