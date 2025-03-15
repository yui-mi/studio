package com.example.yui.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "topics")
@Data
@EqualsAndHashCode(callSuper = false)
public class Topic extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "topic_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String path;

	@Column(nullable = false, length = 1000)
	private String description;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@Column(name = "reservation_date")
	private String reservationDate;

	@Column(name = "reservation_time")
	private String reservationTime;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;

	@OneToMany
	@JoinColumn(name = "topicId", insertable = false, updatable = false)
	private List<Favorite> favorites;

	@OneToMany
	@JoinColumn(name = "topicId", insertable = false, updatable = false)
	private List<Comment> comments;
}