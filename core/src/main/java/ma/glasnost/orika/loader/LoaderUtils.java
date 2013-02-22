package ma.glasnost.orika.loader;

/**
 * @author ikozar
 * date     13.02.13
 */
public class LoaderUtils {
    private LoaderUtils() {
    }

    public static  <E extends Enum<E>>E findLocalPart(String localPart, Class<E> enumToCheckForMatch)
    {
        for (E enumVal : enumToCheckForMatch.getEnumConstants())
        {
            if (((ILocalPart) enumVal).getLocalPart().equals(localPart))
            {
                return enumVal;
            }
        }
        return null;
    }

    public static boolean areAllNull(Object ... values) {
        if (values != null) {
            for (Object val : values) {
                if (val != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean areAllNotNull(Object ... values) {
        if (values != null) {
            for (Object val : values) {
                if (val == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
