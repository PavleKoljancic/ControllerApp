package test.designe.app.controllerapp.models;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RouteHistoryPrimaryKey  {



    Timestamp fromDateTime;


    Integer routeId;

    Integer terminalId;
}
