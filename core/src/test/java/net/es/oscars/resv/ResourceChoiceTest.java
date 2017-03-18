package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.helpers.ResourceChooser;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class ResourceChoiceTest extends AbstractCoreTest {
    @Autowired
    private ResourceChooser chooser;

    @Test
    @Category(QuickTests.class)
    public void testOneChoice() throws ResourceChooser.ResourceChoiceException {
        Set<Integer> reserved = new HashSet<>();
        reserved.add(1);
        reserved.add(2);
        Optional<Integer> choice = chooser.chooseInRange(1, 100, reserved, ResourceChooser.Method.SEQUENTIAL);
        assert choice.isPresent();
        assert choice.get().equals(3);

    }

}
