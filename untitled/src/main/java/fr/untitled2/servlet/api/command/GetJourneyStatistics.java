package fr.untitled2.servlet.api.command;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import fr.untitled2.business.JourneyBusiness;
import fr.untitled2.common.entities.JourneyStatistic;
import fr.untitled2.common.entities.JourneysStatistics;
import fr.untitled2.common.entities.raspi.SimpleStringMessage;
import fr.untitled2.entities.JourneyEntity;
import fr.untitled2.entities.User;
import fr.untitled2.common.utils.DistanceUtils;
import org.javatuples.Pair;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 04/12/13
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class GetJourneyStatistics extends Command<SimpleStringMessage, JourneysStatistics, JourneysStatistics> {

    private static JourneyBusiness journeyBusiness = new JourneyBusiness();

    @Override
    protected JourneysStatistics status(User user) throws Exception {
        return getJourneys(user);
    }

    @Override
    protected JourneysStatistics execute(SimpleStringMessage input, User user, String fromIpAddress) throws Exception {
        return getJourneys(user);
    }

    private JourneysStatistics getJourneys(final User user) {
        final PeriodFormatter periodFormat = PeriodFormat.wordBased(new Locale(user.getLocale()));
        final DecimalFormat decimalFormat = new DecimalFormat("0.##");

        JourneysStatistics result = new JourneysStatistics();
        result.getStatistics().addAll(Lists.newArrayList(Iterables.transform(Ordering.natural().reverse().onResultOf(new Function<JourneyEntity, LocalDateTime>() {
            @Override
            public LocalDateTime apply(JourneyEntity journeyEntity) {
                return journeyEntity.getStartDatetime();
            }
        }).sortedCopy(journeyBusiness.getUserJourneys(user)), new Function<JourneyEntity, JourneyStatistic>() {
            @Override
            public JourneyStatistic apply(JourneyEntity journeyEntity) {
                Period duration = new Period(journeyEntity.getStartDatetime(), journeyEntity.getEndDateTime());
                JourneyStatistic journeyStatistic = new JourneyStatistic();
                journeyStatistic.setJourneyId(journeyEntity.getId());
                journeyStatistic.setDistance(decimalFormat.format(journeyEntity.getDistance() / 1000));
                journeyStatistic.setMaxSpeed(decimalFormat.format(journeyEntity.getMaxSpeed() * 3.6));
                journeyStatistic.setAverageSpeed(decimalFormat.format((journeyEntity.getDistance() / duration.toStandardSeconds().getSeconds()) * 3.6));
                journeyStatistic.setEnd(journeyEntity.getEnd().getName());
                journeyStatistic.setPointCount(journeyEntity.getPointCount());
                journeyStatistic.setStart(journeyEntity.getStart().getName());
                journeyStatistic.setTimeZone(journeyEntity.getDateTimeZone());
                journeyStatistic.setFlightAverageSpeed((DistanceUtils.getDistance(journeyEntity.getStart().getLatitudeLongitude(), journeyEntity.getEnd().getLatitudeLongitude()) / duration.toStandardSeconds().getSeconds()) * 3.6);
                journeyStatistic.setDuration(periodFormat.print(duration.withMillis(0).withSeconds(0)));
                journeyStatistic.setDate(DateTimeFormat.forPattern(user.getDateFormat()).print(journeyEntity.getStartDatetime()));
                return journeyStatistic;
            }
        })));
        return result;
    }

    @Override
    protected Class<SimpleStringMessage> getInputObjectType() {
        return SimpleStringMessage.class;
    }

    @Override
    protected Class<JourneysStatistics> getOutputObjectType() {
        return JourneysStatistics.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
