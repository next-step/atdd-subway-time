package atdd.docs;

import atdd.favorite.application.dto.CreateFavoriteStationRequestView;
import atdd.favorite.domain.FavoriteStation;
import atdd.favorite.domain.FavoriteStationRepository;
import atdd.path.dao.StationDao;
import atdd.path.domain.Station;
import atdd.user.domain.User;
import atdd.user.domain.UserRepository;
import atdd.user.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static atdd.Constant.AUTH_SCHEME_BEARER;
import static atdd.path.TestConstant.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfig.class)
public class FavoriteStationDocumentationTest {
    public static final String FAVORITE_STATION_BASE_URI = "/favorite-stations";
    public static final String NAME = "brown";
    public static final String EMAIL = "boorwonie@email.com";
    public static final String PASSWORD = "subway";
    private Station station1;
    private Station station2;
    private Station station3;
    private String token;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FavoriteStationRepository favoriteStationRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    StationDao stationDao;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.save(new User(NAME, EMAIL, PASSWORD));
        station1 = stationDao.save(new Station(STATION_NAME_20));
        station2 = stationDao.save(new Station(STATION_NAME_21));
        station3 = stationDao.save(new Station(STATION_NAME_19));
        token = jwtTokenProvider.createToken(EMAIL);
    }

    @AfterEach
    void clean() {
        userRepository.deleteAll();
        stationDao.deleteById(station1.getId());
        stationDao.deleteById(station2.getId());
        stationDao.deleteById(station3.getId());
    }

    @Test
    public void createFavoriteStationTest() throws Exception {
        //given
        CreateFavoriteStationRequestView requestView
                = new CreateFavoriteStationRequestView(station1.getId());
        String inputJson = objectMapper.writeValueAsString(requestView);

        //when, then
        mockMvc.perform(
                post(FAVORITE_STATION_BASE_URI)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_SCHEME_BEARER + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("userEmail").exists())
                .andExpect(jsonPath("favoriteStationId").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.favorite-station-showAllStations").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(print())
                .andDo(document("favorite-station-create"));
    }

    @Test
    public void deleteFavoriteStationTest() throws Exception {
        //given
        favoriteStationRepository.save(new FavoriteStation(EMAIL, station1.getId()));

        //when, then
        mockMvc.perform(
                delete(FAVORITE_STATION_BASE_URI + "/" + station1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_SCHEME_BEARER + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(print())
                .andDo(document("favorite-station-delete"));
    }

    @Test
    public void showAllFavoriteStations() throws Exception {
        //given
        favoriteStationRepository.save(new FavoriteStation(EMAIL, station1.getId()));
        favoriteStationRepository.save(new FavoriteStation(EMAIL, station2.getId()));
        favoriteStationRepository.save(new FavoriteStation(EMAIL, station3.getId()));

        //when, then
        mockMvc.perform(
                get(FAVORITE_STATION_BASE_URI)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_SCHEME_BEARER + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("favoriteStations[2].id").value(station3.getId()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(print())
                .andDo(document("favorite-station-showAllFavoriteStations"));
    }
}