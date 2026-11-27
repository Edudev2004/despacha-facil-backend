package com.despachafacil.api.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType = "RETAIL";

    @Column(name = "whatsapp_number", nullable = false, length = 30)
    private String whatsappNumber;

    @Column(nullable = false, length = 10)
    private String currency = "PEN";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_has_border", nullable = false)
    private Boolean logoHasBorder = true;

    @Column(name = "primary_color", length = 10)
    private String primaryColor = "#FFA800";

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "yape_qr_url", length = 500)
    private String yapeQrUrl;

    @Column(name = "yape_phone", length = 30)
    private String yapePhone;

    @Column(name = "plin_phone", length = 30)
    private String plinPhone;

    @Column(name = "free_shipping_threshold", precision = 10, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Tenant() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Boolean getLogoHasBorder() { return logoHasBorder; }
    public void setLogoHasBorder(Boolean logoHasBorder) { this.logoHasBorder = logoHasBorder; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getYapeQrUrl() { return yapeQrUrl; }
    public void setYapeQrUrl(String yapeQrUrl) { this.yapeQrUrl = yapeQrUrl; }
    public String getYapePhone() { return yapePhone; }
    public void setYapePhone(String yapePhone) { this.yapePhone = yapePhone; }
    public String getPlinPhone() { return plinPhone; }
    public void setPlinPhone(String plinPhone) { this.plinPhone = plinPhone; }
    public BigDecimal getFreeShippingThreshold() { return freeShippingThreshold; }
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) { this.freeShippingThreshold = freeShippingThreshold; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
