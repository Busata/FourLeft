package io.busata.fourleft.api.models.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ViewPointsTo {

    List<SinglePointListTo> points;
}
