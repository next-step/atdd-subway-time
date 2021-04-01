package nextstep.subway.path.application;

import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.PathType;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.path.exception.DoesNotConnectedPathException;
import nextstep.subway.path.exception.SameStationPathSearchException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import nextstep.subway.station.exception.StationNonExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("지하철 경로 조회 비즈니스 로직 단위 테스트")
@SpringBootTest
@Transactional
public class PathServiceTest {

    private static final long ADULT_MEMBER_ID = 1L;
    private static final long YOUTH_MEMBER_ID = 2L;
    private static final long CHILD_MEMBER_ID = 3L;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private LineService lineService;

    @Autowired
    private PathService pathService;

    private Station savedStationGangNam;
    private Station savedStationYeokSam;
    private Station savedStationYangJae;
    private Station savedStationYangJaeCitizensForest;
    private Station savedStationCheonggyesan;
    private Station savedStationGyoDae;
    private Station savedStationNambuTerminal;

    private LineRequest line2Request;
    private LineRequest line3Request;
    private LineRequest lineNewBunDang;

    @BeforeEach
    void setUp() {
        savedStationGangNam = stationRepository.save(new Station("강남역"));
        savedStationYeokSam = stationRepository.save(new Station("역삼역"));
        savedStationYangJae = stationRepository.save(new Station("양재역"));
        savedStationYangJaeCitizensForest = stationRepository.save(new Station("양재시민의숲역"));
        savedStationCheonggyesan = stationRepository.save(new Station("청계산입구역"));
        savedStationGyoDae = stationRepository.save(new Station("교대역"));
        savedStationNambuTerminal = stationRepository.save(new Station("남부터미널역"));

        line2Request = new LineRequest("2호선", "bg-green-600", savedStationGyoDae.getId(), savedStationGangNam.getId(), 7, 7);
        LineResponse line2Response = lineService.saveLine(line2Request);
        lineService.addSectionToLine(line2Response.getId(), createSectionRequest(savedStationGangNam, savedStationYeokSam, 5, 5));


        line3Request = new LineRequest("3호선", "bg-orange-600", savedStationGyoDae.getId(), savedStationNambuTerminal.getId(), 3, 3);
        LineResponse line3Response = lineService.saveLine(line3Request);
        lineService.addSectionToLine(line3Response.getId(), createSectionRequest(savedStationNambuTerminal, savedStationYangJae, 3, 3));

        lineNewBunDang = new LineRequest("신분당선", "bg-red-600", savedStationGangNam.getId(), savedStationYangJae.getId(), 5, 5);
        lineNewBunDang.addExtraCharge(900);
        LineResponse lineNewBunDangResponse = lineService.saveLine(lineNewBunDang);
        lineService.addSectionToLine(lineNewBunDangResponse.getId(), createSectionRequest(savedStationYangJae, savedStationYangJaeCitizensForest, 3, 3));
        lineService.addSectionToLine(lineNewBunDangResponse.getId(), createSectionRequest(savedStationYangJaeCitizensForest, savedStationCheonggyesan, 4, 7));
    }

    @Test
    @DisplayName("비로그인 사용자 지하철 최단 거리 경로 조회")
    void findShortestPathDistance() {
        // given
        long source = savedStationGangNam.getId();
        long target = savedStationNambuTerminal.getId();

        // when
        PathResponse pathResponse = pathService.findPath(source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(3);
        assertThat(pathResponse.getDistance()).isEqualTo(10);
        assertThat(pathResponse.getFare()).isEqualTo(1250);
    }

    @Test
    @DisplayName("로그인 사용자(성인) 지하철 최단 거리 경로 조회")
    void findShortestPathDistanceAndAdultAge() {
        // given
        long source = savedStationGangNam.getId();
        long target = savedStationNambuTerminal.getId();

        // when
        PathResponse pathResponse = pathService.findPath(ADULT_MEMBER_ID, source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(3);
        assertThat(pathResponse.getDistance()).isEqualTo(10);
        assertThat(pathResponse.getFare()).isEqualTo(1250);
    }

    @Test
    @DisplayName("지하철 최소 시간 경로 조회")
    void findShortestPathByDuration() {
        // given
        long source = savedStationGangNam.getId();
        long target = savedStationNambuTerminal.getId();

        // when
        PathResponse pathResponse = pathService.findPath(source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(3);
        assertThat(pathResponse.getDuration()).isEqualTo(10);
    }

    @Test
    @DisplayName("지하철 최단 거리 경로 조회 - 추가요금이 있는 신분당선을 경유할 경우 (10Km 이하)")
    void findPathByDistanceToNewBunDang1() {
        // given
        long source = savedStationYangJae.getId();
        long target = savedStationCheonggyesan.getId();

        // when
        PathResponse pathResponse = pathService.findPath(source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(3);
        assertThat(pathResponse.getDuration()).isEqualTo(10);
        assertThat(pathResponse.getFare()).isEqualTo(2150);
    }

    @Test
    @DisplayName("로그인 사용자(청소년) 지하철 최단 거리 경로 조회 - 추가요금이 있는 신분당선을 경유할 경우 (10Km 이하)")
    void findPathByDistanceAndYouthAge() {
        // given
        long source = savedStationYangJae.getId();
        long target = savedStationCheonggyesan.getId();

        // when
        PathResponse pathResponse = pathService.findPath(YOUTH_MEMBER_ID, source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(3);
        assertThat(pathResponse.getDuration()).isEqualTo(10);
        assertThat(pathResponse.getFare()).isEqualTo(2150);
    }

    @Test
    @DisplayName("지하철 최단 거리 경로 조회 - 추가요금이 있는 신분당선을 경유할 경우 (10Km 초과)")
    void findPathByDistanceToNewBunDang2() {
        // given
        long source = savedStationYeokSam.getId();
        long target = savedStationYangJaeCitizensForest.getId();

        // when
        PathResponse pathResponse = pathService.findPath(source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(4);
        assertThat(pathResponse.getDuration()).isEqualTo(13);
        assertThat(pathResponse.getFare()).isEqualTo(2250);
    }

    @Test
    @DisplayName("로그인 사용자(어린이) 지하철 최단 거리 경로 조회 - 추가요금이 있는 신분당선을 경유할 경우 (10Km 초과)")
    void findPathByDistanceAndChildAge() {
        // given
        long source = savedStationYeokSam.getId();
        long target = savedStationYangJaeCitizensForest.getId();

        // when
        PathResponse pathResponse = pathService.findPath(CHILD_MEMBER_ID, source, target, PathType.DISTANCE);

        // then
        assertThat(pathResponse.getStations()).hasSize(4);
        assertThat(pathResponse.getDuration()).isEqualTo(13);
        assertThat(pathResponse.getFare()).isEqualTo(2250);
    }

    @Test
    @DisplayName("출발역과 도착역이 같은 경우 예외 발생")
    void notEqualsSourceAndTarget() {
        // given
        long source = savedStationGangNam.getId();
        long target = savedStationGangNam.getId();

        // when & then
        assertThatExceptionOfType(SameStationPathSearchException.class)
                .isThrownBy(() -> pathService.findPath(source, target, PathType.DISTANCE));
    }

    @Test
    @DisplayName("출발역과 도착역이 연결되어 있지 않은 경우 예외 발생")
    void notConnectedSourceAndTarget() {
        // given
        Station savedStationMyeongDong = stationRepository.save(new Station("명동역"));

        long source = savedStationGangNam.getId();
        long target = savedStationMyeongDong.getId();

        // when & then
        assertThatExceptionOfType(DoesNotConnectedPathException.class)
                .isThrownBy(() -> pathService.findPath(source, target, PathType.DISTANCE));
    }

    @Test
    @DisplayName("존재하지 않는 출발역, 도착역을 조회할 경우 예외 발생")
    void findNotExistSourceAndTarget() {
        // given
        long source = 100L;
        long target = 101L;

        // when & then
        assertThatExceptionOfType(StationNonExistException.class)
                .isThrownBy(() -> pathService.findPath(source, target, PathType.DISTANCE));
    }

    private SectionRequest createSectionRequest(Station upStation, Station downStation, int distance, int duration) {
        return new SectionRequest(upStation.getId(), downStation.getId(), distance, duration);
    }
}
