package atdd.path.application;

import atdd.TestConstant;
import atdd.path.dao.FavoritePathDao;
import atdd.path.dao.StationDao;
import atdd.path.domain.FavoritePath;
import atdd.user.application.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static atdd.TestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = FavoritePathService.class)
public class FavoritePathServiceTest {
    @Autowired
    private FavoritePathService favoritePathService;

    @MockBean
    private FavoritePathDao favoritePathDao;
    @MockBean
    private UserDao userDao;
    @MockBean
    private StationDao stationDao;

    @Test
    public void addFavoritePath() {
        given(userDao.findByEmail(any())).willReturn(TestConstant.TEST_USER_2);
        given(stationDao.findById(TEST_STATION.getId())).willReturn(TEST_STATION);
        given(stationDao.findById(TEST_STATION_4.getId())).willReturn(TEST_STATION_4);
        given(favoritePathDao.save(any())).willReturn(FAVORITE_PATH_1);

        FavoritePath favoritePath = favoritePathService.addFavoritePath(FAVORITE_PATH_1);

        assertThat(favoritePath.getSourceStationId()).isEqualTo(TEST_STATION.getId());
        assertThat(favoritePath.getTargetStationId()).isEqualTo(TEST_STATION_4.getId());
    }

    @Test
    public void findFavoritePath() {
        given(userDao.findByEmail(any())).willReturn(TEST_USER_2);
        given(stationDao.findById(TEST_STATION.getId())).willReturn(TEST_STATION);
        given(stationDao.findById(TEST_STATION_4.getId())).willReturn(TEST_STATION_4);
        given(favoritePathDao.findAll(TEST_USER_2.getId())).willReturn(Arrays.asList(FAVORITE_PATH_1));

        List<FavoritePath> favoritePaths = favoritePathService.findFavoritePath(TEST_USER_2.getId());

        assertThat(favoritePaths.size()).isEqualTo(1);
        assertThat(favoritePaths.get(0).getSourceStationId()).isEqualTo(TEST_STATION.getId());
        assertThat(favoritePaths.get(0).getTargetStationId()).isEqualTo(TEST_STATION_4.getId());
    }
}