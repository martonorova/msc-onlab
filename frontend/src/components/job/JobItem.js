import React, { useContext } from 'react';
import { Descriptions, Space, Button } from 'antd';

import JobContext from '../../context/job/jobContext';

const JobItem = ({ item }) => {
  const jobContext = useContext(JobContext);
  const { postJob } = jobContext;

  const onRedo = () => {
    postJob({ input: item.input });
  };

  return (
    <div
      style={{
        width: '100%',
        display: 'flex',
        justifyContent: 'space-between',
      }}
    >
      <Descriptions bordered size='small'>
        <Descriptions.Item label='ID'>{item.id}</Descriptions.Item>
        <Descriptions.Item label='Input'>{item.input}</Descriptions.Item>
        <Descriptions.Item label='Result'>
          <strong>{item.result}</strong>
        </Descriptions.Item>
      </Descriptions>

      <Button type='primary' onClick={onRedo}>
        REDO
      </Button>
    </div>
  );
};

export default JobItem;
