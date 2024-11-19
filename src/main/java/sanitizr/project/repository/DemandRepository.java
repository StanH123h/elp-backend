package sanitizr.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sanitizr.project.entity.DemandEntity;

import java.util.Date;
import java.util.List;

@Repository
public interface DemandRepository extends CrudRepository<DemandEntity,Integer> {
    public List<DemandEntity> findAllByTimeAfter(Date time);
    public Integer countAllByTimeAfterAndPeriodAndToiletId(Date time, Integer period,String toiletId);
    public List<DemandEntity> findAllByTimeBetween(Date beginTime, Date endTime);
    public Integer countAllByTimeBetweenAndToiletIdAndDayAndPeriod(Date beginTime, Date endTime, String toiletId, Integer day, Integer period);
    public Integer countAllByDayAndPeriodAndToiletId(Integer day, Integer period, String toiletId);
    @Query("SELECT DISTINCT a.toiletId from DemandEntity a")
    public List<String> findAllToiletId();
    public Integer countAllByPeriodAndToiletId(Integer period, String toiletId);
}
