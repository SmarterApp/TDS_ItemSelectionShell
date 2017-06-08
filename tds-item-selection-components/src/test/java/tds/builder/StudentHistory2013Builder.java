package tds.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.sets.CsetGroupString;
import tds.itemselection.loader.StudentHistory2013;

public final class StudentHistory2013Builder {
  private String customPool = null;
  private List<CsetGroupString> groups;
  private Map<String, String> excludeGroups;
  private Map<String, ItemResponse> responses;
  private Float startAbility = 0F;
  private Double _startSE = 0D;
  private Double _startInformation = 0D;
  private ArrayList<HashSet<String>> _previousTestItemGroups = new ArrayList<>();
  private HashSet<String> _previousFieldTestItemGroups = new HashSet<>();
  private ArrayList<ItemResponse> _previousResponses = new ArrayList<>();
  private ArrayList<String> _itemPool = new ArrayList<>();

  public StudentHistory2013Builder withCustomPool(String customPool) {
    this.customPool = customPool;
    return this;
  }

  public StudentHistory2013Builder withGroups(List<CsetGroupString> groups) {
    this.groups = groups;
    return this;
  }

  public StudentHistory2013Builder withExcludeGroups(Map<String, String> excludeGroups) {
    this.excludeGroups = excludeGroups;
    return this;
  }

  public StudentHistory2013Builder withResponses(Map<String, ItemResponse> responses) {
    this.responses = responses;
    return this;
  }

  public StudentHistory2013Builder withStartAbility(Float startAbility) {
    this.startAbility = startAbility;
    return this;
  }

  public StudentHistory2013Builder withStartSE(Double _startSE) {
    this._startSE = _startSE;
    return this;
  }

  public StudentHistory2013Builder withStartInformation(Double _startInformation) {
    this._startInformation = _startInformation;
    return this;
  }

  public StudentHistory2013Builder withPreviousTestItemGroups(ArrayList<HashSet<String>> _previousTestItemGroups) {
    this._previousTestItemGroups = _previousTestItemGroups;
    return this;
  }

  public StudentHistory2013Builder withPreviousFieldTestItemGroups(HashSet<String> _previousFieldTestItemGroups) {
    this._previousFieldTestItemGroups = _previousFieldTestItemGroups;
    return this;
  }

  public StudentHistory2013Builder withPreviousResponses(ArrayList<ItemResponse> _previousResponses) {
    this._previousResponses = _previousResponses;
    return this;
  }

  public StudentHistory2013Builder withItemPool(List<String> _itemPool) {
    ArrayList<String> itemPool = new ArrayList<>();
    itemPool.addAll(_itemPool);
    this._itemPool = itemPool;
    return this;
  }

  public StudentHistory2013 build() {
    StudentHistory2013 studentHistory2013 = new StudentHistory2013();
    studentHistory2013.setCustomPool(customPool);
    studentHistory2013.setGroups(groups);
    studentHistory2013.setExcludeGroups(excludeGroups);
    studentHistory2013.setResponses(responses);
    studentHistory2013.setStartAbility(startAbility);
    studentHistory2013.set_startSE(_startSE);
    studentHistory2013.set_startInformation(_startInformation);
    studentHistory2013.set_previousTestItemGroups(_previousTestItemGroups);
    studentHistory2013.set_previousFieldTestItemGroups(_previousFieldTestItemGroups);
    studentHistory2013.set_previousResponses(_previousResponses);
    studentHistory2013.set_itemPool(_itemPool);
    return studentHistory2013;
  }
}
