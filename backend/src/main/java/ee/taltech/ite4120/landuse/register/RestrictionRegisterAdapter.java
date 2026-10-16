package ee.taltech.ite4120.landuse.register;

/**
 * The port through which the application asks "which restrictions lie on this
 * plot?". The restrictions are owned by the national restrictions register
 * (KPOIS, {@code kyKitsendusedAvalik}); we only ever read them, never keep a copy.
 *
 * <p>The rest of the application depends only on this interface. Which transport
 * answers — the built-in imitated register or a real one — is configuration
 * ({@code landuse.restriction-register.url}), not code. Same shape as
 * {@code animals.ownerregistry.OwnerRegistryAdapter}.
 */
public interface RestrictionRegisterAdapter {

    /**
     * The restrictions on one plot.
     *
     * @return the plot, or {@code null} when the register does not know it
     * @throws RestrictionRegisterException when the register cannot be reached or
     *         answers with a fault — the check fails closed on it (LANDUSE.01)
     */
    RegisterPlot restrictions(String cadastralCode);
}
