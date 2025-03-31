package org.gradle.execution.commandline;

import org.gradle.api.Task;
import org.gradle.api.internal.tasks.options.OptionDescriptor;
import org.gradle.api.internal.tasks.options.OptionReader;
import org.gradle.internal.typeconversion.TypeConversionException;
import org.gradle.cli.CommandLineArgumentException;
import org.gradle.cli.CommandLineOption;
import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;
import org.gradle.cli.ParsedCommandLineOption;

import java.util.Collection;
import java.util.List;

public class CommandLineTaskConfigurer {

    private OptionReader optionReader;

    public CommandLineTaskConfigurer(OptionReader optionReader) {
        this.optionReader = optionReader;
    }

    public List<String> configureTasks(Collection<Task> tasks, List<String> arguments) {
        assert !tasks.isEmpty();
        if (arguments.isEmpty()) {
            return arguments;
        }
        return configureTasksNow(tasks, arguments);
    }

    private List<String> configureTasksNow(Collection<Task> tasks, List<String> arguments) {
        List<String> remainingArguments = null;
        for (Task task : tasks) {
            CommandLineParser parser = new CommandLineParser();
            final List<OptionDescriptor> commandLineOptions = optionReader.getOptions(task);
            for (OptionDescriptor optionDescriptor : commandLineOptions) {
                String optionName = optionDescriptor.getName();
                CommandLineOption option = parser.option(optionName);
                option.hasDescription(optionDescriptor.getDescription());
                option.hasArgument(optionDescriptor.getArgumentType());
            }

            ParsedCommandLine parsed;
            try {
                parsed = parser.parse(arguments);
            } catch (CommandLineArgumentException e) {
                //we expect that all options must be applicable for each task
                throw new TaskConfigurationException(task.getPath(), "Problem configuring task " + task.getPath() + " from command line.", e);
            }

            for (OptionDescriptor commandLineOptionDescriptor : commandLineOptions) {
                final String name = commandLineOptionDescriptor.getName();
                if (parsed.hasOption(name)) {
                    ParsedCommandLineOption o = parsed.option(name);
                    try {
                        commandLineOptionDescriptor.apply(task, o.getValues());
                    } catch (TypeConversionException ex) {
                        throw new TaskConfigurationException(task.getPath(),
                                String.format("Problem configuring option '%s' on task '%s' from command line.", name, task.getPath()), ex);
                    }
                }
            }
            assert remainingArguments == null || remainingArguments.equals(parsed.getExtraArguments())
                    : "we expect all options to be consumed by each task so remainingArguments should be the same for each task";
            remainingArguments = parsed.getExtraArguments();
        }
        return remainingArguments;
    }
}
