package algalon.logon.realm;
import algalon.database.Account;
import algalon.logon.Version;
import java.util.ArrayList;
import java.util.function.Consumer;
import kotlin.jvm.functions.Function1;

public final class Realm
{
    // ---------------------------------------------------------------------------------------------

    public static final ArrayList<Realm> list = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    public Realm(Consumer<Realm> init)
    {
        init.accept(this);
        if (display_name == null) display_name = name;
        ip_string = ip + ":" + port;
    }

    // ---------------------------------------------------------------------------------------------

    /** Unique realm id. */
    public int id = 0;

    // ---------------------------------------------------------------------------------------------

    /** Displayed name of the realm. */
    public String name;

    // ---------------------------------------------------------------------------------------------

    /** Name to display to the client in the realm list. */
    public String display_name;

    // ---------------------------------------------------------------------------------------------

    /** The IP address of this realm's server. */
    public String ip;

    // ---------------------------------------------------------------------------------------------

    /** The port of this realm's server. */
    public int port;

    // ---------------------------------------------------------------------------------------------

    /** "IP:PORT" */
    public String ip_string;

    // ---------------------------------------------------------------------------------------------

    /** A conjunction of {@link RealmFlag}. */
    public int flags;

    // ---------------------------------------------------------------------------------------------

    /** Checks whether the realm accepts connections from a client with the given version. */
    public Function1<Version, Boolean> version;

    // ---------------------------------------------------------------------------------------------

    /** Checks wether the realm appears locked to the given user (v2+). */
    public Function1<Account, Boolean> locked = (account) -> false;

    // ---------------------------------------------------------------------------------------------

    // TODO cross realms?
    /**
     * - Represent the server's time zone (these are values out of an enum).
     * - 0 for cross-realms, we hard-code this value here.
     * - Could be used to exclude some localized clients (see Packets.txt).
     */
    public int timezone = 0;

    // ---------------------------------------------------------------------------------------------

    /** Normal, PvP, ... */
    public RealmType type;

    // ---------------------------------------------------------------------------------------------

    /** Population level (high, medium, low). */
    public RealmPopulation population;

    // ---------------------------------------------------------------------------------------------

    public Realm set (RealmFlag flag)
    {
        flags |= flag.getValue();
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public Realm set (RealmFlag... flags)
    {
        for (RealmFlag flag: flags)
            this.flags |= flag.getValue();
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    public boolean has (RealmFlag flag)
    {
        return (flags & flag.getValue()) != 0;
    }

    // ---------------------------------------------------------------------------------------------
}
