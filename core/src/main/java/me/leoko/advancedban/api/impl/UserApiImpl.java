package me.leoko.advancedban.api.impl;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.api.UserApi;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserApiImpl implements UserApi {
    @Override
    public List<Punishment> fetchActivePunishments(UUID uuid) throws SQLException {
        return this.fetchPunishments(uuid, SQLQuery.SELECT_USER_PUNISHMENTS);
    }

    @Override
    public List<Punishment> fetchHistoryPunishments(UUID uuid) throws SQLException {
        return this.fetchPunishments(uuid, SQLQuery.SELECT_USER_PUNISHMENTS_HISTORY);
    }

    private List<Punishment> fetchPunishments(UUID uuid, SQLQuery sqlQuery) throws SQLException {
        MethodInterface mi = Universal.get().getMethods();
        final String internUUID = mi.getInternUUID(uuid);
        final List<Punishment> punishmentList = new ArrayList<>();

        try (ResultSet result = DatabaseManager.get().executeResultStatement(sqlQuery, internUUID)) {
            if (result != null) {
                while (result.next()) {
                    punishmentList.add(PunishmentManager.get().getPunishmentFromResultSet(result));
                }
            }
        }

        return punishmentList;
    }
}
