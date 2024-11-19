package sanitizr.project.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sanitizr.project.entity.DemandEntity;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public interface DemandService {
    public List<DemandEntity> getAll();
    public List<DemandEntity> getAllByTimeBetween(Date beginTime, Date endTime);
    public List<String> getAllToiletId();
    Integer countAllByDayAndPeriodAndToiletId(Integer day, Integer period, String toiletId);
    Integer countAllByDayAndPeriodAndTimeBetweenAndToiletId(Integer day, Integer period, Date beginTime, Date endTime, String toiletId);
    public void addDemand(Date time, Integer day, Integer period, String toiletId);
    public Integer countAllByPeriodAndToiletId(Integer period, String toiletId);
    public Integer countAllByTimeAfterAndPeriodAndToiletId(Date time, Integer period, String toiletId);
}
