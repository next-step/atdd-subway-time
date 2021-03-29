package nextstep.subway.line.acceptance.linesection;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static nextstep.subway.line.acceptance.line.LineRequestSteps.지하철_노선_생성_요청;
import static nextstep.subway.line.acceptance.linesection.LineSectionRequestSteps.*;
import static nextstep.subway.line.acceptance.linesection.LineSectionVerificationSteps.*;
import static nextstep.subway.station.acceptance.StationRequestSteps.지하철_역_등록_됨;

@DisplayName("지하철 노선에 역 등록 관련 기능")
public class LineSectionAcceptanceTest extends AcceptanceTest {

    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 양재시민의숲역;
    private StationResponse 청계산입구역;
    private StationResponse 판교역;

    private LineResponse 신분당선;

    @BeforeEach
    public void init() {
        super.setUp();

        // given
        강남역 = 지하철_역_등록_됨("강남역").as(StationResponse.class);
        양재역 = 지하철_역_등록_됨("양재역").as(StationResponse.class);
        양재시민의숲역 = 지하철_역_등록_됨("양재시민의숲역").as(StationResponse.class);
        청계산입구역 = 지하철_역_등록_됨("청계산입구역").as(StationResponse.class);
        판교역 = 지하철_역_등록_됨("판교역").as(StationResponse.class);

        신분당선 = 지하철_노선_생성_요청(노선_요청("신분당선", "bg-red-600", 양재역.getId(), 청계산입구역.getId(), 7, 10))
                .as(LineResponse.class);
    }

    @Test
    @DisplayName("지하철 노선에 등록된 구간에 새로운 상행 역을 등록한다.")
    void addUpStationLineSection() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 강남역.getId(), 양재역.getId(), 5, 5);

        // then
        지하철_노선에_구간_등록_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 등록된 구간 사이에 역을 추가 등록한다.")
    void addBetweenLineSection() {
        // when
        지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 양재시민의숲역.getId(), 3, 3);
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 판교역.getId(), 양재시민의숲역.getId(), 1, 1);

        // then
        지하철_노선에_구간_등록_됨(response);
        지하철_노선에_등록된_구간_이름_확인(response, Arrays.asList(양재역, 판교역, 양재시민의숲역, 청계산입구역));
    }

    @Test
    @DisplayName("지하철 노선에 새로운 하행 구간을 등록한다.")
    void addDownStationLineSection() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 청계산입구역.getId(), 판교역.getId(), 16, 13);

        // then
        지하철_노선에_구간_등록_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 추가하는 구간의 거리가 기존노선의 거리보다 크면 등록할 수 없다.")
    void invalidAddSectionBecauseDistanceLarger() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 양재시민의숲역.getId(), 10, 10);

        // then
        지하철_노선에_구간_등록_실패_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 추가하는 구간의 거리가 기존노선의 거리보다 같으면 등록할 수 없다.")
    void invalidAddSectionBecauseDistanceEquals() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 양재시민의숲역.getId(), 7, 3);

        // then
        지하철_노선에_구간_등록_실패_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 추가하는 구간의 상행역과 하행역이 모두 존재하면 등록할 수 없다.")
    void invalidAddSectionBecauseExistStation() {
        // given
        지하철_노선에_구간_등록_요청(신분당선.getId(), 청계산입구역.getId(), 판교역.getId(), 16, 13);

        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 판교역.getId(), 23, 23);

        // then
        지하철_노선에_구간_등록_실패_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 이미 포함된 역을 구간으로 등록할 수 없다.")
    void invalidAddLineSectionAlreadyIncluded() {
        // given
        지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 청계산입구역.getId(), 7, 10);

        // when
        ExtractableResponse<Response> response = 지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 청계산입구역.getId(), 7, 10);

        // then
        지하철_노선에_구간_등록_실패_됨(response);
    }

    @Test
    @DisplayName("지하철 노선에 등록된 상행 종점역을 제거한다.")
    void removeUpStationLineSection() {
        // given
        지하철_노선에_구간_등록_요청(신분당선.getId(), 양재역.getId(), 양재시민의숲역.getId(), 3, 3);

        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선에_등록된_구간_제거_요청(신분당선.getId(), 양재역.getId());

        // then
        지하철_노선에_등록된_구간_제거_됨(deleteResponse);
    }

    @Test
    @DisplayName("지하철 노선에 등록된 중간 노선 역을 제거한다.")
    void removeMiddleStationLineSection() {
        // given
        지하철_노선에_구간_등록_요청(신분당선.getId(), 청계산입구역.getId(), 판교역.getId(), 16, 13);

        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선에_등록된_구간_제거_요청(신분당선.getId(), 청계산입구역.getId());

        // then
        지하철_노선에_등록된_구간_제거_됨(deleteResponse);
    }

    @Test
    @DisplayName("지하철 노선에 등록된 하행 종점역을 제거한다.")
    void removeDownStationLineSection() {
        // given
        지하철_노선에_구간_등록_요청(신분당선.getId(), 청계산입구역.getId(), 판교역.getId(), 16, 13).as(LineResponse.class);

        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선에_등록된_구간_제거_요청(신분당선.getId(), 판교역.getId());

        // then
        지하철_노선에_등록된_구간_제거_됨(deleteResponse);
    }

    @Test
    @DisplayName("지하철 노선에 구간이 하나혹은 없을때 지하철역을 제거 할 수 없다.")
    void removeLineSectionOnlyOneSection() {
        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선에_등록된_구간_제거_요청(신분당선.getId(), 청계산입구역.getId());

        // then
        지하철_노선에_등록된_구간_제거_실패_됨(deleteResponse);
    }
}