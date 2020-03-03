package atdd.path.application;


import atdd.path.dao.FavoritePathDao;
import atdd.path.dao.StationDao;
import atdd.path.domain.FavoritePath;
import atdd.path.domain.Station;
import atdd.user.application.dao.UserDao;
import atdd.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritePathService {
    private FavoritePathDao favoritePathDao;
    private UserDao userDao;
    private StationDao stationDao;

    public FavoritePathService(FavoritePathDao favoritePathDao, UserDao userDao, StationDao stationDao) {
        this.favoritePathDao = favoritePathDao;
        this.userDao = userDao;
        this.stationDao = stationDao;
    }

    public FavoritePath addFavoritePath(final String email, FavoritePath favoritePath) {
        User user = userDao.findByEmail(email);

        favoritePath.setOwner(user.getId());

        Station sourceStation = stationDao.findById(favoritePath.getSourceStationId());
        Station tartStation = stationDao.findById(favoritePath.getTargetStationId());

        long favoritePathId = favoritePathDao.save(favoritePath).getId();

        favoritePath.setId(favoritePathId);
        favoritePath.setSourceStation(sourceStation);
        favoritePath.setTargetStation(tartStation);

        return favoritePath;
    }

    public List<FavoritePath> findFavoritePath(final String email) {
        User user = userDao.findByEmail(email);

        List<FavoritePath> favoritePaths = favoritePathDao.findAll(user.getId());
        for (FavoritePath favoritePath : favoritePaths) {
            favoritePath.setSourceStation(stationDao.findById(favoritePath.getSourceStationId()));
            favoritePath.setTargetStation(stationDao.findById(favoritePath.getTargetStationId()));
        }

        return favoritePaths;
    }

    public void deleteFavoritePath(final String email, final long id) {
        User user = userDao.findByEmail(email);

        favoritePathDao.deleteById(user.getId(), id);
    }
}
