package ee.taltech.ite4120.landuse.register.http.wire;

import java.math.BigDecimal;

/**
 * WIRE RECORD — one restriction inside {@link KyKitsendusedResponse}, in the
 * register's own vocabulary.
 *
 * @param nahtuseLiik   the register's restriction type, e.g. {@code ELEKTRIPAIGALDISE_KAITSEVOOND}
 * @param nimi          name of the restriction
 * @param seaduslikAlus legal basis
 * @param kpoOmanik     owner of the restricting object
 * @param pindala       area of the plot the restriction covers, m²
 */
public record KyKitsendus(
        String nahtuseLiik,
        String nimi,
        String seaduslikAlus,
        String kpoOmanik,
        BigDecimal pindala) {}
