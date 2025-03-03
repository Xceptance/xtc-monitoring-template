package com.xceptance.ordermonitoring.model.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.jayway.jsonpath.JsonPath;
import com.xceptance.ordermonitoring.model.configuration.dataobject.Requirement;
import com.xceptance.ordermonitoring.model.configuration.dataobject.TimeBasedRequirement;
import com.xceptance.ordermonitoring.model.configuration.dataobject.TimeBasedRequirement.TimeBasedRequirementBuilder;
import com.xceptance.ordermonitoring.model.configuration.dataobject.Timeframe;

public class PropertiesHelper
{
    public static List<TimeBasedRequirement> getTheMostRelevantTimebasedRequirementForTestClass(final Class<?> testClass)
    {
        return getTheMostRelevantTimebasedRequirementsForTestClass(testClass, LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC")));
    }

    public static List<TimeBasedRequirement> getTheMostRelevantTimebasedRequirementsForTestClass(final Class<?> testClass,
                                                                                                 final ZonedDateTime now)
    {
        final File source = new File(getPathToPropertiesFile());
        List<TimeBasedRequirement> timebasedRequirements = new ArrayList<TimeBasedRequirement>();
        try
        {
            if (source.exists())
            {
                FileInputStream fileInputStream;

                fileInputStream = new FileInputStream(source);
                final Properties properties = new Properties();
                properties.load(fileInputStream);
                fileInputStream.close();
                final List<String> targetConditions = getRequiredStringListPropertyViaKey(testClass.getCanonicalName()+"."+ParserHelper.conditionKey,ParserHelper.conditionsSeparator, properties);
                for (String targetCondition : targetConditions)
                {
                    final String targetSite = getRequiredStringPropertyViaKey(targetCondition+"."+ ParserHelper.siteKey,properties);
                    final String targetTimezone = getOptionalStringPropertyViaKey(targetCondition+"."+ ParserHelper.timezoneKey,properties).orElse("UTC");                 
                    TimeBasedRequirementBuilder timeBasedRequirementBuilder = new TimeBasedRequirementBuilder().setConsideredPeriod(Long.parseLong(getRequiredStringPropertyViaKey(targetCondition+"."+ ParserHelper.consideredPeriodKey,properties)))
                                                                                                               .setOrderStatuses(getOptionalStringPropertyViaKey(targetCondition+"."+ ParserHelper.orderStatuses,properties).orElse(""))
                                                                                                               .setPaymentMethods(getOptionalStringPropertyViaKey(targetCondition+"."+ ParserHelper.paymentMethods,properties).orElse(""))
                                                                                                               .setPathToUniqueAttribute(getOptionalStringListPropertyViaKey(targetCondition+"."+ ParserHelper.pathToUniqueAttributeKey,ParserHelper.pathToUniqueAttributeKeyArraySeparator,properties))
                                                                                                               .setMaxTotalOrderNumberToIgnoreConditon(Integer.valueOf(getOptionalStringPropertyViaKey(targetCondition+"."+ ParserHelper.maxTotalOrderNumberToIgnoreConditonKey, properties).orElse("0")))
                                                                                                               .setSite(targetSite)
                                                                                                               .setTargetLocale(getOptionalStringPropertyViaKey(targetCondition+"."+ ParserHelper.localeKey,properties).orElse(targetSite.replaceAll(".*-", "")))
                                                                                                               .setTimeZone(targetTimezone);
                    
                    final ZonedDateTime zonedNow = now.withZoneSameInstant(ZoneId.of(targetTimezone));
                    final JavaPropsMapper javaPropsMapper = JavaPropsMapper.builder().build();
                    final JsonNode targetCondtionInJsonFormat = javaPropsMapper.readPropertiesAs(properties, JsonNode.class)
                                                                               .get(targetCondition);   
                    if (targetCondtionInJsonFormat == null)
                    {
                        throw new RuntimeException("No configurations found for condition " + targetCondition);
                    }else {
                    	timeBasedRequirementBuilder.setCustomConditions(getCustomConditions(targetCondtionInJsonFormat));
                    }

                    final Optional<TimeBasedRequirement> exclusivePeriodRequirement = getTimeBasedRequirementForExclusivePeriodIfPresent(timeBasedRequirementBuilder,
                                                                                                                                         zonedNow,
                                                                                                                                         targetCondtionInJsonFormat);
                    final Optional<TimeBasedRequirement> dayOfTheWeekRequirement = getTimeBasedRequirementForDayOfTheWeekIfPresent(
                                                                                                                                   timeBasedRequirementBuilder,
                                                                                                                                   zonedNow,
                                                                                                                                   targetCondtionInJsonFormat);
                    final Optional<TimeBasedRequirement> defaultRequirement = getDefaultTimeBasedRequirementIfPresent(
                                                                                                                      timeBasedRequirementBuilder, zonedNow,
                                                                                                                      targetCondtionInJsonFormat);
                    if (exclusivePeriodRequirement.isEmpty() && dayOfTheWeekRequirement.isEmpty()
                        && defaultRequirement.isEmpty())
                    {
                        throw new RuntimeException("No requirement found for time " + zonedNow+" for condition "+targetCondition);
                    }
                    final List<TimeBasedRequirement> consideredRefinements = new ArrayList<TimeBasedRequirement>();
                    if (defaultRequirement.isPresent())
                    {
                        consideredRefinements.add(defaultRequirement.get());
                    }
                    if (dayOfTheWeekRequirement.isPresent())
                    {
                        consideredRefinements.add(dayOfTheWeekRequirement.get());
                    }
                    if (exclusivePeriodRequirement.isPresent())
                    {
                        consideredRefinements.add(exclusivePeriodRequirement.get());
                    }
                    timebasedRequirements.add(mergeRequirements(consideredRefinements));
                }
            }
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
        return timebasedRequirements;
    }
    
    private static String getPathToPropertiesFile()
    {
        File customProperties = new File("../../../../tmp/custom.properties");
        // File customProperties = new File("config/custom.properties");
        if (!customProperties.exists())
        {
            return "./config/order-schedule.properties";
        }
        File mergedProperties = new File("./config/order-schedule-merged.properties");
        mergedProperties.deleteOnExit();
        try (FileOutputStream output = new FileOutputStream(mergedProperties))
        {
            Files.copy(new File("./config/order-schedule.properties").toPath(), output);
            Files.copy(customProperties.toPath(), output);
            System.out.println("Merged properties file");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(mergedProperties))))
            {
                String line = r.readLine();
                while (line != null)
                {
                    System.out.println(line);
                    line = r.readLine();
                }
            }
            System.out.println("-------------------------");
            System.out.println("custom properties file");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(customProperties))))
            {
                String line = r.readLine();
                while (line != null)
                {
                    System.out.println(line);
                    line = r.readLine();
                }
            }
            System.out.println("-------------------------");
            System.out.println("original properties file");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./config/order-schedule.properties")))))
            {
                String line = r.readLine();
                while (line != null)
                {
                    System.out.println(line);
                    line = r.readLine();
                }
            }
            System.out.println("-------------------------");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return mergedProperties.getPath();
    }


    private static Optional<String> getOptionalStringPropertyViaKey(String key, final Properties properties){
        final Object targetObject = properties.get( key);
        return targetObject != null && StringUtils.isNotBlank(targetObject.toString()) ? Optional.of(targetObject.toString()) : Optional.empty();
    }
    
    private static String getRequiredStringPropertyViaKey(String key, final Properties properties){
    	Optional<String> result = getOptionalStringPropertyViaKey(key, properties);
    	if(result.isEmpty()) {
            throw new RuntimeException("No "+key+" defined");
    	}
    	return result.get();
    }
    
    private static List<String> getOptionalStringListPropertyViaKey(String key, String separator,final Properties properties)
    {
    	Optional<String> result = getOptionalStringPropertyViaKey(key, properties);
        return !result.isEmpty()? List.of(result.get().split(separator)) : new ArrayList<String>();
    }
    
    private static List<String> getRequiredStringListPropertyViaKey(String key, String separator,final Properties properties)
    {
    	List<String> result = getOptionalStringListPropertyViaKey(key, separator, properties);
    	if(result.isEmpty()) {
            throw new RuntimeException("No "+key+" property is defined");
    	}
    	return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, String>> getCustomConditions(final JsonNode request)
    {
        if (request.get(ParserHelper.customConditionKey) != null)
        {
            return ((Map<String, Map<String, String>>) JsonPath.parse(request.toString()).read("$."+ParserHelper.customConditionKey));

        }
        return new HashMap<String, Map<String, String>>();
    }

    private static Optional<TimeBasedRequirement> getTimeBasedRequirementForExclusivePeriodIfPresent(
                                                                                                     TimeBasedRequirementBuilder timeBasedRequirementBuilder,
                                                                                                     final ZonedDateTime now,
                                                                                                     final JsonNode request)
    {
        if (request.get(ParserHelper.exclusivePeriodKey) != null)
        {
            @SuppressWarnings("rawtypes")
            final List<Map<String, Map>> targetExclusivePeriods = JsonPath.parse(request.toString())
                                                                          .read("$." + ParserHelper.exclusivePeriodKey + ".*[?]",
                                                                                new DatePredicate(now.toLocalDate()));
            final List<TimeBasedRequirement> applicableTimebasedRequirements = targetExclusivePeriods.stream()
                                                                                                     .flatMap(m -> m
                                                                                                                    .entrySet().stream()
                                                                                                                    .map(e -> e.getValue() instanceof Map
                                                                                                                                                          ? timeBasedRequirementBuilder.setRequirement(new ObjectMapper().convertValue(e.getValue(),
                                                                                                                                                                                                                                       Requirement.class))
                                                                                                                                                                                       .setTimeframe(Timeframe.parseString(e.getKey()))
                                                                                                                                                                                       .build()
                                                                                                                                                          : null)
                                                                                                                    .filter(e -> e instanceof TimeBasedRequirement
                                                                                                                                 && e.isActiveNow(now.toLocalTime())))
                                                                                                     .collect(Collectors.toList());

            if (applicableTimebasedRequirements.size() == 0)
            {
                return Optional.empty();
            }
            if (applicableTimebasedRequirements.size() > 0)
            {
                return Optional.of(mergeRequirements(applicableTimebasedRequirements));
            }
            else
            {
                return Optional.of(applicableTimebasedRequirements.get(0));
            }
        }
        return Optional.empty();
    }

    private static Optional<TimeBasedRequirement> getTimeBasedRequirementForDayOfTheWeekIfPresent(
                                                                                                  TimeBasedRequirementBuilder timeBasedRequirementBuilder,
                                                                                                  final ZonedDateTime now,
                                                                                                  final JsonNode request)
    {
        final String targetDayOfTheWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.US)
                                             .toLowerCase();
        return getTimeBasedRequirementForKeyIfPresent(targetDayOfTheWeek, timeBasedRequirementBuilder,
                                                      now, request);
    }

    private static Optional<TimeBasedRequirement> getDefaultTimeBasedRequirementIfPresent(
                                                                                          TimeBasedRequirementBuilder timeBasedRequirementBuilder,
                                                                                          final ZonedDateTime now,
                                                                                          final JsonNode request)
    {
        return getTimeBasedRequirementForKeyIfPresent(ParserHelper.everydayKey, timeBasedRequirementBuilder, now, request);
    }

    private static Optional<TimeBasedRequirement> getTimeBasedRequirementForKeyIfPresent(final String key,
                                                                                         TimeBasedRequirementBuilder timeBasedRequirementBuilder,
                                                                                         final ZonedDateTime now,
                                                                                         final JsonNode request)
    {
        if (request.get(key) != null)
        {
            @SuppressWarnings("rawtypes")
            final Map<String, Map> targetTimeframes = JsonPath.parse(request.toString()).read("$." + key);

            List<TimeBasedRequirement> applicableTimebasedRequirements = targetTimeframes.entrySet().stream()
                                                                                         .filter(e -> !e.getKey().equals("default"))
                                                                                         .map(e -> timeBasedRequirementBuilder.setRequirement(new ObjectMapper().convertValue(e.getValue(),
                                                                                                                                                                              Requirement.class))
                                                                                                                              .setTimeframe(Timeframe.parseString(e.getKey()))
                                                                                                                              .build())
                                                                                         .filter(e -> e.isActiveNow(now.toLocalTime()))
                                                                                         .collect(Collectors.toList());
            if (applicableTimebasedRequirements.size() == 0)
            {
                applicableTimebasedRequirements = targetTimeframes.entrySet().stream()
                                                                  .filter(e -> e.getKey().equals("default"))
                                                                  .map(e -> timeBasedRequirementBuilder.setRequirement(new ObjectMapper().convertValue(e.getValue(),
                                                                                                                                                       Requirement.class))
                                                                                                       .setTimeframe(Timeframe.parseString(e.getKey()))
                                                                                                       .build())
                                                                  .filter(e -> e.isActiveNow(now.toLocalTime()))
                                                                  .collect(Collectors.toList());

            }

            if (applicableTimebasedRequirements.size() == 0)
            {
                return Optional.empty();
            }
            if (applicableTimebasedRequirements.size() > 0)
            {
                return Optional.of(mergeRequirements(applicableTimebasedRequirements));
            }
            else
            {
                return Optional.of(applicableTimebasedRequirements.get(0));
            }
        }
        return Optional.empty();
    }

    private static TimeBasedRequirement mergeRequirements(
                                                         final List<TimeBasedRequirement> sortedApplicableTimebasedRequirements)
    {
        TimeBasedRequirement base = sortedApplicableTimebasedRequirements
                                                                         .get(sortedApplicableTimebasedRequirements.size() - 1);
        for (int i = sortedApplicableTimebasedRequirements.size() - 2; i >= 0; i--)
        {
            base = base.merge(sortedApplicableTimebasedRequirements.get(i));
        }
        return base;
    }
}
