package it.warehouse.optimization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Date;

@MappedSuperclass
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public abstract class AbstractAuditable extends Model {

    @JsonIgnore
    @Version
    @Schema(hidden = true)
    protected Long _version;

    @JsonIgnore
    @WhenCreated

    @Schema(hidden = true)
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date _dataCreazione;

    @JsonIgnore
    @WhoCreated
    @Schema(hidden = true)
    @Column(updatable = false)
    protected String _utenteCreazione;

    @JsonIgnore
    @WhenModified
    @Schema(hidden = true)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date _dataModifica;

    @JsonIgnore
    @WhoModified
    @Schema(hidden = true)
    protected String _utenteModifica;
}