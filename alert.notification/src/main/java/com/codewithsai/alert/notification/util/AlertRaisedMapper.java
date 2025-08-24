package com.codewithsai.alert.notification.util;

import com.codewithsai.alert.notification.model.AlertRaised;
import com.codewithsai.alert.notification.model.entity.AlertRaisedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


@Mapper(componentModel = "spring")
public interface AlertRaisedMapper {

    AlertRaisedMapper INSTANCE = Mappers.getMapper(AlertRaisedMapper.class);

    AlertRaisedEntity toEntity(AlertRaised alert);

    AlertRaised fromEntity(AlertRaisedEntity entity);
}

