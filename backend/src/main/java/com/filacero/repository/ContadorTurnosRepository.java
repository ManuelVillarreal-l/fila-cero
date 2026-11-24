package com.filacero.repository;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Consecutivo diario de turnos por sucursal y trámite (RN1). Usa findAndModify con $inc,
 * que en MongoDB es atómico: dos clientes nunca reciben el mismo número.
 */
@Repository
public class ContadorTurnosRepository {

    private static final String COLECCION = "contadores";
    private final MongoTemplate mongo;

    public ContadorTurnosRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @SuppressWarnings("unchecked")
    public int siguiente(String sucursalId, String tramiteId, String fecha) {
        String clave = sucursalId + ":" + tramiteId + ":" + fecha;
        Map<String, Object> doc = mongo.findAndModify(
                Query.query(Criteria.where("_id").is(clave)),
                new Update().inc("valor", 1),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                Map.class, COLECCION);
        return ((Number) doc.get("valor")).intValue();
    }
}
