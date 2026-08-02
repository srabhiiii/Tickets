package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;

/**
 * TICKET-ADV051 — JPA entity Instrument. JSONB metadata column wired via
 * the Hypersistence Utils JsonBinaryType on Postgres; H2 stores it as a
 * plain CLOB via the dialect translation (acceptable for dev).
 */
@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "asset_class", nullable = false, length = 20)
    private String assetClass;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 12)
    private String isin;

    public Instrument() {}

    public Long getId()         { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol()   { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName()     { return name; }
    public void setName(String name) { this.name = name; }
    public String getAssetClass(){ return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getIsin()     { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
}
