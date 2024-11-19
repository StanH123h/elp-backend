package sanitizr.project.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sanitizr.project.entity.DemandEntity;
import sanitizr.project.repository.DemandRepository;
import sanitizr.project.service.DemandService;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DemandServiceImpl implements DemandService {
    DemandRepository demandRepository;

    DemandServiceImpl(DemandRepository demandRepository) {
        this.demandRepository = demandRepository;
    }

    @Override
    public List<DemandEntity> getAll() {
        return (List<DemandEntity>) demandRepository.findAll();
    }

    @Override
    public List<DemandEntity> getAllByTimeBetween(Date beginTime, Date endTime) {
        return demandRepository.findAllByTimeBetween(beginTime, endTime);
    }

    @Override
    public List<String> getAllToiletId() {
        return demandRepository.findAllToiletId();
    }

    @Override
    public Integer countAllByDayAndPeriodAndToiletId(Integer day, Integer period, String toiletId) {
        return demandRepository.countAllByDayAndPeriodAndToiletId(day,period,toiletId);
    }

    @Override
    public Integer countAllByDayAndPeriodAndTimeBetweenAndToiletId(Integer day, Integer period, Date beginTime, Date endTime, String toiletId) {
        return demandRepository.countAllByTimeBetweenAndToiletIdAndDayAndPeriod(beginTime, endTime, toiletId, day, period);
    }

    @Override
    public void addDemand(Date time, Integer day, Integer period, String toiletId) {
        DemandEntity demandEntity = new DemandEntity();
        demandEntity.setDay(day);
        demandEntity.setTime(time);
        demandEntity.setPeriod(period);
        demandEntity.setToiletId(toiletId);
        demandRepository.save(demandEntity);
    }

    @Override
    public Integer countAllByPeriodAndToiletId(Integer period, String toiletId) {
        return demandRepository.countAllByPeriodAndToiletId(period,toiletId);
    }

    @Override
    public Integer countAllByTimeAfterAndPeriodAndToiletId(Date time,Integer period,String toiletId) {
        return demandRepository.countAllByTimeAfterAndPeriodAndToiletId(time,period,toiletId);
    }


}
