import React, { useContext, useEffect } from 'react';
import { Button, List, Space } from 'antd';

import JobContext from '../../context/job/jobContext';
import JobItem from '../job/JobItem';

const JobList = () => {
  const jobContext = useContext(JobContext);

  const { jobs, getJobs, postJob, loading } = jobContext;

  useEffect(() => {
    getJobs();
    // eslint-disable-next-line
  }, []);

  return (
    <List
      dataSource={jobs}
      loading={loading}
      bordered
      renderItem={(item) => (
        <List.Item title={item.id}>
          <JobItem item={item} />
        </List.Item>
      )}
    />
  );
};

export default JobList;
