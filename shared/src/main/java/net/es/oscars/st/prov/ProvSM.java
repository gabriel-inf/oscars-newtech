package net.es.oscars.st.prov;


public class ProvSM {

    private ProvState state = ProvState.INITIAL;

    public ProvState next(ProvEvent event) throws ProvStateException {
        String msg = "event: " + event.toString() + " arrived at state: " + state.toString();
        switch (state) {
            case INITIAL:
                switch (event) {
                    case SWITCH_TO_AUTO:
                        return ProvState.DISMANTLED_AUTO;
                    case SWITCH_TO_MANUAL:
                        return ProvState.DISMANTLED_MANUAL;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case DISMANTLED_MANUAL:
                switch (event) {
                    case SWITCH_TO_AUTO:
                        return ProvState.DISMANTLED_AUTO;
                    case SWITCH_TO_MANUAL:
                        return ProvState.DISMANTLED_MANUAL;
                    case BUILD_COMMAND:
                        return ProvState.BUILDING_MANUAL;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case DISMANTLED_AUTO:
                switch (event) {
                    case SWITCH_TO_AUTO:
                        return ProvState.DISMANTLED_AUTO;
                    case SWITCH_TO_MANUAL:
                        return ProvState.DISMANTLED_MANUAL;
                    case BUILD_TIME_REACHED:
                        return ProvState.BUILDING_AUTO;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case BUILDING_MANUAL:
                switch (event) {
                    case BUILD_OK:
                        return ProvState.BUILT_MANUAL;
                    case BUILD_FL:
                        return ProvState.FAILED;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case BUILDING_AUTO:
                switch (event) {
                    case BUILD_OK:
                        return ProvState.BUILT_AUTO;
                    case BUILD_FL:
                        return ProvState.FAILED;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case BUILT_MANUAL:
                switch (event) {
                    case SWITCH_TO_AUTO:
                        return ProvState.BUILT_AUTO;
                    case SWITCH_TO_MANUAL:
                        return ProvState.BUILT_MANUAL;
                    case DISMANTLE_COMMAND:
                        return ProvState.DISMANTLING_MANUAL;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }
            case BUILT_AUTO:
                switch (event) {
                    case SWITCH_TO_AUTO:
                        return ProvState.BUILT_AUTO;
                    case SWITCH_TO_MANUAL:
                        return ProvState.BUILT_MANUAL;
                    case DISMANTLE_TIME_REACHED:
                        return ProvState.DISMANTLING_AUTO;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case DISMANTLING_MANUAL:
                switch (event) {
                    case DISMANTLE_OK:
                        return ProvState.DISMANTLED_MANUAL;
                    case DISMANTLE_FL:
                        return ProvState.FAILED;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case DISMANTLING_AUTO:
                switch (event) {
                    case DISMANTLE_OK:
                        return ProvState.DISMANTLED_AUTO;
                    case DISMANTLE_FL:
                        return ProvState.FAILED;
                    default:
                        throw new ProvStateException("not allowed: "+msg);
                }

            case FAILED:
                throw new ProvStateException("not allowed: "+msg);

            default:
                throw new ProvStateException("not allowed: "+msg);
        }
    }
}
