package com.portal.jobms.job.impl;

import com.portal.jobms.job.clients.CompanyClient;
import com.portal.jobms.job.clients.ReviewClient;
import com.portal.jobms.job.Job;
import com.portal.jobms.job.JobRepository;
import com.portal.jobms.job.JobService;
import com.portal.jobms.job.dto.JobDTO;
import com.portal.jobms.job.external.Company;
import com.portal.jobms.job.external.Review;
import com.portal.jobms.job.mapper.JobMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

//    private List<Job> jobs = new ArrayList<>();
    JobRepository jobRepository;
//    @Autowired
//    RestTemplate restTemplate;

    private CompanyClient companyClient;
    private ReviewClient reviewClient;

    public JobServiceImpl(JobRepository jobRepository,CompanyClient companyClient, ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }


    @Override
//    @CircuitBreaker(name="companyBreaker",fallbackMethod = "companyBreakerFallBack")
//    @Retry(name="companyBreaker",fallbackMethod = "companyBreakerFallBack")
    @RateLimiter(name="companyBreaker",fallbackMethod = "companyBreakerFallBack")
    public List<JobDTO> findAll() {

        List<Job> jobs = jobRepository.findAll();

        return jobs.stream().map(this::convertToDto).toList();
    }

    public List<String> companyBreakerFallBack(Throwable t){
        List<String> list = new ArrayList<>();
        list.add("Please try after some time");

        return list;
    }

    private JobDTO convertToDto(Job job) {
//        Company company = restTemplate.getForObject(
//                "http://company-service:8081/companies/"+job.getCompanyId(), Company.class
//        );
        Company company = companyClient.getCompany(job.getCompanyId());
        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());
//        ResponseEntity<List<Review>> reviewResponse = restTemplate.exchange("http://review-service:8083/reviews?companyId=" + job.getCompanyId(),
//                HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
//                });
//        List<Review> reviews = reviewResponse.getBody();
        return JobMapper.mapToJobWithCompanyDTO(job,company,reviews);
    }

    @Override
    public void createJob(Job job) {
        jobRepository.save(job);
    }

    @Override
    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        return convertToDto(job);
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            jobRepository.deleteById(id);
            return true;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
            if(jobOptional.isPresent()){
                Job job = jobOptional.get();
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                job.setLocation(updatedJob.getLocation());
                job.setMinSalary(updatedJob.getMinSalary());
                job.setMaxSalary(updatedJob.getMaxSalary());
                jobRepository.save(job);
                return true;
            }

        return false;
    }
}
