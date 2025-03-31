package org.gradle.model;

import org.gradle.api.GradleException;
import org.gradle.api.Incubating;
import org.gradle.model.internal.core.ModelPath;
import org.gradle.model.internal.core.rule.describe.ModelRuleDescriptor;
import org.gradle.model.internal.type.ModelType;

/**
 * Thrown when an attempt is made to change the value of a model element that is not writable at
 * the time.
 */
@Incubating
public class ReadOnlyModelViewException extends GradleException {
    public ReadOnlyModelViewException(String message) {
        super(message);
    }

    public ReadOnlyModelViewException(ModelPath path,
                                      ModelType<?> type,
                                      ModelRuleDescriptor ruleDescriptor) {
        super(createMessage("read only", path, type, ruleDescriptor));
    }

    protected static String createMessage(String viewType,
                                          ModelPath path,
                                          ModelType<?> type,
                                          ModelRuleDescriptor ruleDescriptor) {
        StringBuilder result = new StringBuilder();
        result.append("Attempt to modify a ").append(viewType).append(" view of model element '");
        result.append(path);
        result.append("'");
        if (!type.equals(ModelType.UNTYPED)) {
            result.append(" of type '");
            result.append(type.getDisplayName());
            result.append("'");
        }
        result.append(" given to rule ");
        ruleDescriptor.describeTo(result);
        return result.toString();
    }
}
