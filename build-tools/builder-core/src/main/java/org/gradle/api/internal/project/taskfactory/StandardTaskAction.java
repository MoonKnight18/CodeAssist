package org.gradle.api.internal.project.taskfactory;

import org.gradle.api.Describable;
import org.gradle.api.Task;
import org.gradle.internal.execution.history.changes.InputChangesInternal;
import org.gradle.internal.hash.ClassLoaderHierarchyHasher;
import org.gradle.internal.reflect.JavaMethod;
import org.gradle.internal.snapshot.impl.ImplementationSnapshot;
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction;

import java.lang.reflect.Method;

class StandardTaskAction implements InputChangesAwareTaskAction, Describable {
    private final Class<? extends Task> type;
    private final Method method;

    public StandardTaskAction(Class<? extends Task> type, Method method) {
        this.type = type;
        this.method = method;
    }

    @Override
    public void setInputChanges(InputChangesInternal inputChanges) {
    }

    @Override
    public void clearInputChanges() {
    }

    @Override
    public void execute(Task task) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(method.getDeclaringClass().getClassLoader());
        try {
            doExecute(task, method.getName());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    protected void doExecute(Task task, String methodName) {
        JavaMethod.of(task, Object.class, methodName).invoke(task);
    }

    @Override
    public ImplementationSnapshot getActionImplementation(ClassLoaderHierarchyHasher hasher) {
        return ImplementationSnapshot.of(type.getName(), hasher.getClassLoaderHash(method.getDeclaringClass().getClassLoader()));
    }

    @Override
    public String getDisplayName() {
        return "Execute " + method.getName();
    }
}