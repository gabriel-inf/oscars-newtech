package net.es.oscars.helpers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
public class ResourceChooser {
    public enum Method {
        RANDOM,
        SEQUENTIAL
    }

    public class ResourceChoiceException extends Exception {
        public ResourceChoiceException(String msg) {
            super(msg);
        }

    }

    public Optional<Integer> chooseInRange(Integer origin, Integer bound, Set<Integer> reserved, Method method)
            throws IllegalArgumentException, ResourceChoiceException {

        if (bound < origin) {
            throw new IllegalArgumentException("bound must be >= than origin!");
        }

        int rangeSize = bound - origin;
        Set<Integer> reservedInRange = new HashSet<>();
        for (Integer i : reserved) {
            if (i >= origin && i < bound) {
                reservedInRange.add(i);
            }
        }

        if (reservedInRange.size() >= rangeSize) {
            return Optional.empty();
        } else if (reservedInRange.size() * 2 > rangeSize && method.equals(Method.RANDOM)) {
            log.info("overriding random method; pool is too small");
            method = Method.SEQUENTIAL;
        }

        Optional<Integer> result = Optional.empty();
        boolean found = false;
        for (Integer i = origin; i < bound; i++) {
            if (!reserved.contains(i) && !found) {
                result = Optional.of(i);
                found = true;
                log.info("decided minimal is: "+i);
            }
        }

        if (!result.isPresent()) {
            throw new ResourceChoiceException("could not find a minimal unreserved int, internal error?");
        }

        if (method == Method.RANDOM) {
            Random r = new Random();
            int attempts = 0;
            while (attempts < rangeSize * 10) {
                int i = origin + r.nextInt(rangeSize);
                if (!reservedInRange.contains(i)) {
                    return Optional.of(i);
                }
            }
        } else {
            return result;
        }
        throw new ResourceChoiceException("could not find an unreserved int, internal error?");

    }

}
